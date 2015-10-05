package com.mysticwind.disabledappmanager.domain.state;

import com.mysticwind.disabledappmanager.domain.timer.TimerManager;
import com.mysticwind.disabledappmanager.domain.timer.TimesUpObserver;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimerTriggeredDisabledPackageStateDecider
        implements DisabledPackageStateDecider, TimesUpObserver {
    private final TimerManager timerManager;
    private final Map<String, DisabledStateDetectionRequest> packageNameToRequestMap;
    private String previousOnScreenPackageName;
    private final Set<DecisionObserver> decisionObservers = new HashSet<>();

    public TimerTriggeredDisabledPackageStateDecider(TimerManager timerManager) {
        this.timerManager = timerManager;
        this.packageNameToRequestMap = new ConcurrentHashMap<>();
    }

    @Override
    public void registerDecisionObserver(DecisionObserver decisionObserver) {
        synchronized (decisionObservers) {
            decisionObservers.add(decisionObserver);
        }
    }

    @Override
    public void unregisterDecisionObserver(DecisionObserver decisionObserver) {
        synchronized (decisionObservers) {
            decisionObservers.remove(decisionObserver);
        }
    }

    @Override
    public void addDetectionRequest(DisabledStateDetectionRequest disabledStateDetectionRequest) {
        addPackageDisableRequest(disabledStateDetectionRequest);
        scheduleTimer(
                disabledStateDetectionRequest.getPackageName(),
                disabledStateDetectionRequest.getNoActivityTimeoutInSeconds());
    }

    private void addPackageDisableRequest(DisabledStateDetectionRequest request) {
        packageNameToRequestMap.put(request.getPackageName(), request);
    }

    private void scheduleTimer(String packageName, long timeoutInSeconds) {
        log.debug("Scheduling timer task for " + packageName);
        timerManager.schedule(this, packageName, timeoutInSeconds);
    }

    @Override
    public void windowSwitchedTo(String packageName) {
        log.debug("Switched to " + packageName);
        if (isPackageRequestedToDisable(packageName)) {
            cancelTimer(packageName);
        }
        if (previousOnScreenPackageName == null) {
            previousOnScreenPackageName = packageName;
        }
        packageSwitched(previousOnScreenPackageName, packageName);
    }

    private boolean isPackageRequestedToDisable(String packageName) {
        return packageNameToRequestMap.containsKey(packageName);
    }

    private void cancelTimer(String packageName) {
        try {
            timerManager.cancel(packageName);
        // we receive events for each different activity of that package
        } catch (Exception e) {
            return;
        }
        log.debug("Cancelled timer for " + packageName);
    }

    private void packageSwitched(String previousOnScreenPackageName, String currentOnScreenPackageName) {
        if (previousOnScreenPackageName.equals(currentOnScreenPackageName)) {
            return;
        }
        if (isPackageRequestedToDisable(previousOnScreenPackageName)) {
            rescheduleTimer(previousOnScreenPackageName);
        }
        this.previousOnScreenPackageName = currentOnScreenPackageName;
    }

    private void rescheduleTimer(String packageName) {
        DisabledStateDetectionRequest request = packageNameToRequestMap.get(packageName);
        scheduleTimer(request.getPackageName(), request.getNoActivityTimeoutInSeconds());
    }

    @Override
    public void timesUp(String packageName) {
        if (!isPackageRequestedToDisable(packageName)) {
            return;
        }
        for (DecisionObserver observer : decisionObservers) {
            observer.update(new StateDecision(packageName, PackageState.DISABLE));
        }
        cancelPackageDisableRequest(packageName);
    }

    private void cancelPackageDisableRequest(String packageName) {
        packageNameToRequestMap.remove(packageName);
    }
}
