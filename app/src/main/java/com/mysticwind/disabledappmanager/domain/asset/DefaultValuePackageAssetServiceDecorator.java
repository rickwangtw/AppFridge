package com.mysticwind.disabledappmanager.domain.asset;

import android.graphics.drawable.Drawable;

public class DefaultValuePackageAssetServiceDecorator implements PackageAssetService {
    private final PackageAssetService packageAssetService;
    private final Drawable defaultIcon;

    public DefaultValuePackageAssetServiceDecorator(
            PackageAssetService packageAssetService, Drawable defaultIcon) {
        this.packageAssetService = packageAssetService;
        this.defaultIcon = defaultIcon;
    }

    @Override
    public PackageAssets getPackageAssets(String packageName) {
        PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
        if (packageAssets != null) {
            return packageAssets;
        }
        return new PackageAssets(packageName, packageName, defaultIcon);
    }

    @Override
    public Drawable getAppIcon(String packageName) {
        return getPackageAssets(packageName).getIconDrawable();
    }

    @Override
    public String getAppName(String packageName) {
        return getPackageAssets(packageName).getAppName();
    }
}
