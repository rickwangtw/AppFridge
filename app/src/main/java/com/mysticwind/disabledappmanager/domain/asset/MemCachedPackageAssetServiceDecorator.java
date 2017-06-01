package com.mysticwind.disabledappmanager.domain.asset;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemCachedPackageAssetServiceDecorator implements PackageAssetService {
    private final PackageAssetService packageAssetService;

    public MemCachedPackageAssetServiceDecorator(final PackageAssetService packageAssetService) {
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
    }

    private LoadingCache<String, PackageAssets> packageAssetsLoadingCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, PackageAssets>() {
                public PackageAssets load(String packageName) {
                    return packageAssetService.getPackageAssets(packageName);
                }
            });

    @Override
    public PackageAssets getPackageAssets(String packageName) {
        try {
            return packageAssetsLoadingCache.get(packageName);
        } catch (Exception e) {
            // do not log the stack as it probably comes from PackageAssetService not being ready
            log.warn("Failed to load package assets for package " + packageName);
            return null;
        }
    }
}
