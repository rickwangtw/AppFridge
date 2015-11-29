package com.mysticwind.disabledappmanager.domain.asset;

import android.graphics.drawable.Drawable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemCachedPackageAssetServiceDecorator implements PackageAssetService {
    private final PackageAssetService packageAssetService;

    public MemCachedPackageAssetServiceDecorator(PackageAssetService packageAssetService) {
        this.packageAssetService = packageAssetService;
    }

    private LoadingCache<String, PackageAssets> packageAssetsLoadingCache = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build(new CacheLoader<String, PackageAssets>() {
                public PackageAssets load(String packageName) {
                    PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
                    if (packageAssets == null) {
                        throw new RuntimeException("Failed to obtain package assets for package name: " + packageName);
                    }
                    return packageAssets;
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

    @Override
    public Drawable getAppIcon(String packageName) {
        PackageAssets packageAssets = getPackageAssets(packageName);
        if (packageAssets == null) {
            return null;
        }
        return packageAssets.getIconDrawable();
    }

    @Override
    public String getAppName(String packageName) {
        PackageAssets packageAssets = getPackageAssets(packageName);
        if (packageAssets == null) {
            return null;
        }
        return packageAssets.getAppName();
    }
}
