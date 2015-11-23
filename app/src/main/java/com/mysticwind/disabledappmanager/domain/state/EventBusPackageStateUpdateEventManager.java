package com.mysticwind.disabledappmanager.domain.state;

import com.mysticwind.disabledappmanager.domain.event.EventBusEventManager;

import de.greenrobot.event.EventBus;

public class EventBusPackageStateUpdateEventManager
        extends EventBusEventManager<PackageStateUpdate, PackageStateUpdateListener>
        implements PackageStateUpdateEventManager {

    public EventBusPackageStateUpdateEventManager(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void publishUpdate(String packageName, PackageState packageState) {
        publishUpdate(new PackageStateUpdate(packageName, packageState));
    }
}
