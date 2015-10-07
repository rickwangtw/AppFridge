package com.mysticwind.disabledappmanager.domain.state;

import com.mysticwind.disabledappmanager.domain.event.EventBusEventManager;

import de.greenrobot.event.EventBus;

public class EventBusManualStateUpdateEventManager
        extends EventBusEventManager<ManualStateUpdate, ManualStateUpdateListener>
        implements ManualStateUpdateEventManager {

    public EventBusManualStateUpdateEventManager(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void publishUpdate(String packageName, PackageState packageState) {
        publishUpdate(new ManualStateUpdate(packageName, packageState));
    }
}
