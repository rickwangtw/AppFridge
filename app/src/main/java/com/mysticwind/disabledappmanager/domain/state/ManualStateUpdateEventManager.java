package com.mysticwind.disabledappmanager.domain.state;

public interface ManualStateUpdateEventManager {
    void registerListener(ManualStateUpdateListener listener);
    void unregisterListener(ManualStateUpdateListener listener);
    void publishUpdate(String packageName, PackageState packageState);
}
