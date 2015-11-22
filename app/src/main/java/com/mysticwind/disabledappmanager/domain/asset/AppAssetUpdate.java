package com.mysticwind.disabledappmanager.domain.asset;

import java.util.Set;

import lombok.Value;

@Value
public class AppAssetUpdate {
    private final String packageName;
    private final Set<AssetType> updatedAssetTypes;
}
