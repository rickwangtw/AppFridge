package com.mysticwind.disabledappmanager.domain;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.ui.common.Action;
import com.mysticwind.disabledappmanager.ui.common.PackageStateUpdateAsyncTask;

import java.util.Arrays;

import de.greenrobot.event.EventBus;

public class PackageManagerAppLauncher implements AppLauncher {
    private final PackageManager packageManager;
    private final AppStateProvider appStateProvider;
    private final PackageStateController packageStateController;

    public PackageManagerAppLauncher(PackageManager packageManager,
                                     AppStateProvider appStateProvider,
                                     PackageStateController packageStateController) {
        this.packageManager = packageManager;
        this.appStateProvider = appStateProvider;
        this.packageStateController = packageStateController;
    }

    @Override
    public void launch(Context context, String packageName) {
        boolean isEnabled = appStateProvider.isPackageEnabled(packageName);
        if (!isEnabled) {
            Toast.makeText(
                    context, context.getResources().getString(
                            R.string.toast_enabled_packages_msg_prefix) + " " + packageName,
                    Toast.LENGTH_SHORT).show();
            packageStateController.enablePackages(Arrays.asList(packageName));
            postPackageStateUpdated();
        }
        Intent intent = packageManager.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            Toast.makeText(context, context.getResources().getString(
                            R.string.toast_warn_no_launcher_intent_msg_prefix) + " " + packageName,
                    Toast.LENGTH_SHORT).show();
            /* disable back if the original status is disabled */
            if (!isEnabled) {
                Toast toast = Toast.makeText(context, context.getResources().getString(
                                R.string.toast_disabled_packages_msg_prefix) + " " + packageName,
                        Toast.LENGTH_SHORT);
                new PackageStateUpdateAsyncTask(
                        packageStateController, appStateProvider, Arrays.asList(packageName), false)
                        .withCompletedEvent(Action.PACKAGE_STATE_UPDATED)
                        .withEndingToast(toast)
                        .execute();
            }
        } else {
            context.startActivity(intent);
        }
    }

    private void postPackageStateUpdated() {
        EventBus.getDefault().post(Action.PACKAGE_STATE_UPDATED);
    }
}
