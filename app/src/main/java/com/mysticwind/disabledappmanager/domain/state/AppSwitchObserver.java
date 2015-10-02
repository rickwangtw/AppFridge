package com.mysticwind.disabledappmanager.domain.state;

public interface AppSwitchObserver {
    void windowSwitchedTo(String packageName);
}
