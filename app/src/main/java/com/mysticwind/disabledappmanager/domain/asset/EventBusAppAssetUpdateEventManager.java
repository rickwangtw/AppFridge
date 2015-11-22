package com.mysticwind.disabledappmanager.domain.asset;

import com.google.common.collect.ImmutableSet;
import com.mysticwind.disabledappmanager.domain.event.EventBusEventManager;

import java.util.Set;

import de.greenrobot.event.EventBus;

public class EventBusAppAssetUpdateEventManager
        extends EventBusEventManager<AppAssetUpdate, AppAssetUpdateListener>
        implements AppAssetUpdateEventManager {

    public EventBusAppAssetUpdateEventManager(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    public void publishUpdate(String packageName, AssetType updatedAssetType) {
        publishUpdate(packageName, ImmutableSet.of(updatedAssetType));

    }

    @Override
    public void publishUpdate(String packageName, Set<AssetType> updatedAssetType) {
        publishUpdate(new AppAssetUpdate(packageName, updatedAssetType));
    }
}
