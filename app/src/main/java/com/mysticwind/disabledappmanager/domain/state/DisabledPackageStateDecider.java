package com.mysticwind.disabledappmanager.domain.state;

public interface DisabledPackageStateDecider extends AppSwitchObserver {
    void addDetectionRequest(DisabledStateDetectionRequest disabledStateDetectionRequest);
}
