package com.mysticwind.disabledappmanager.domain.appgroup;

import com.mysticwind.disabledappmanager.domain.event.EventBusEventManager;

import de.greenrobot.event.EventBus;

public class EventBusAppGroupUpdateEventManager
        extends EventBusEventManager<AppGroupUpdate, AppGroupUpdateListener>
        implements AppGroupUpdateEventManager {

    public EventBusAppGroupUpdateEventManager(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void publishUpdate(String appGroupName, AppGroupOperation operation) {
        publishUpdate(new AppGroupUpdate(appGroupName, operation));
    }
}
