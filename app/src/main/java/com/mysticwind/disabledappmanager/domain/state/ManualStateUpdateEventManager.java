package com.mysticwind.disabledappmanager.domain.state;

import com.mysticwind.disabledappmanager.domain.event.EventManager;

public interface ManualStateUpdateEventManager
        extends EventManager<ManualStateUpdate, ManualStateUpdateListener> {
    void publishUpdate(String packageName, PackageState packageState);
}
