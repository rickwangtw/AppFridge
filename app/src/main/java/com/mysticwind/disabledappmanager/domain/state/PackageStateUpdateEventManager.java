package com.mysticwind.disabledappmanager.domain.state;

import com.mysticwind.disabledappmanager.domain.event.EventManager;

public interface PackageStateUpdateEventManager
        extends EventManager<PackageStateUpdate, PackageStateUpdateListener> {
    void publishUpdate(String packageName, PackageState packageState);
}
