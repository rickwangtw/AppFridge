package com.mysticwind.disabledappmanager.domain.asset;

import com.mysticwind.disabledappmanager.domain.event.EventManager;

import java.util.Set;

public interface AppAssetUpdateEventManager
        extends EventManager<AppAssetUpdate, AppAssetUpdateListener> {
    void publishUpdate(String packageName, AssetType updatedAssetType);
    void publishUpdate(String packageName, Set<AssetType> updatedAssetTypes);
}
