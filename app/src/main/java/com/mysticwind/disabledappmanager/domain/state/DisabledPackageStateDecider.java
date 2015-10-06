package com.mysticwind.disabledappmanager.domain.state;

public interface DisabledPackageStateDecider extends AppSwitchObserver, ManualStateUpdateListener {
    void registerDecisionObserver(DecisionObserver decisionObserver);
    void unregisterDecisionObserver(DecisionObserver decisionObserver);
    void addDetectionRequest(DisabledStateDetectionRequest disabledStateDetectionRequest);
}
