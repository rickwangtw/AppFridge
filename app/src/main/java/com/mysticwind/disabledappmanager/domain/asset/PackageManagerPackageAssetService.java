package com.mysticwind.disabledappmanager.domain.asset;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageManagerPackageAssetService implements PackageAssetService {

    private final PackageManager packageManager;

    public PackageManagerPackageAssetService(final PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public PackageAssets getPackageAssets(String packageName) {
        ApplicationInfo appInfo;
        try {
            appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            log.warn("Failed to get application info for package: " + packageName);
            return null;
        }
        final String appName = appInfo.loadLabel(packageManager).toString();
        final Drawable icon = appInfo.loadIcon(packageManager);
        return new PackageAssets(packageName, appName, icon);
    }
}
