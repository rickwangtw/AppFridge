package com.mysticwind.disabledappmanager.domain.appgroup;

import com.mysticwind.disabledappmanager.domain.event.EventManager;

public interface AppGroupUpdateEventManager
        extends EventManager<AppGroupUpdate, AppGroupUpdateListener> {
    void publishUpdate(String appGroupName, AppGroupOperation operation);
}
