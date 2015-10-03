package com.mysticwind.disabledappmanager.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.google.common.collect.ImmutableList;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.config.AppSwitchDetectionServiceComponent;
import com.mysticwind.disabledappmanager.config.AppSwitchDetectionServiceModule;
import com.mysticwind.disabledappmanager.config.DaggerAppSwitchDetectionServiceComponent;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.state.DecisionObserver;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.DisabledStateDetectionRequest;
import com.mysticwind.disabledappmanager.domain.state.PackageState;
import com.mysticwind.disabledappmanager.domain.state.StateDecision;
import com.mysticwind.disabledappmanager.ui.common.Action;
import com.mysticwind.disabledappmanager.ui.common.PackageStateUpdateAsyncTask;

import de.greenrobot.event.EventBus;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppSwitchDetectionService extends AccessibilityService implements DecisionObserver {
    private DisabledPackageStateDecider disabledPackageStateDecider;
    private PackageStateController packageStateController;
    private AppStateProvider appStateProvider;
    private AppSwitchDetectionServiceComponent component;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
        setServiceInfo(config);

        component = DaggerAppSwitchDetectionServiceComponent.builder()
                .appSwitchDetectionServiceModule(new AppSwitchDetectionServiceModule(this, this))
                .build();

        this.disabledPackageStateDecider = component.disabledPackageStateDecider();
        this.appStateProvider = component.appStateProvider();
        this.packageStateController = component.packageStateController();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            ComponentName componentName = new ComponentName(
                    event.getPackageName().toString(),
                    event.getClassName().toString()
            );

            ActivityInfo activityInfo = tryGetActivity(componentName);
            boolean isActivity = activityInfo != null;
            if (!isActivity) {
                return;
            }
            log.debug("Window state change event: " + componentName.flattenToShortString());
            disabledPackageStateDecider.windowSwitchedTo(event.getPackageName().toString());
        }
    }

    private ActivityInfo tryGetActivity(ComponentName componentName) {
        try {
            return getPackageManager().getActivityInfo(componentName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    public void onInterrupt() {
    }

    @Override
    public void update(StateDecision stateDecision) {
        log.debug(String.format("StateDecision Update: (%s,%s)"
                , stateDecision.getPackageName(), stateDecision.getDecidedState()));
        if (stateDecision.getDecidedState() != PackageState.DISABLE) {
            return;
        }

        String toastMessage = getResources().getString(
                R.string.toast_auto_disable_package_msg_prefix) + " " + stateDecision.getPackageName();
        Toast endingToast = Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT);

        new PackageStateUpdateAsyncTask(
                    packageStateController,
                    appStateProvider,
                    ImmutableList.of(stateDecision.getPackageName()),
                    false)
                .withCompletedEvent(Action.PACKAGE_STATE_UPDATED)
                .withEndingToast(endingToast)
                .execute();
    }

    // EventBus
    public void onEvent(DisabledStateDetectionRequest request) {
        log.debug(String.format("Received disabled state detection request: (%s, %d)",
                request.getPackageName(), request.getNoActivityTimeoutInSeconds()));
        disabledPackageStateDecider.addDetectionRequest(request);
    }
}
