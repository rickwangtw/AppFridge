package com.mysticwind.disabledappmanager.domain.asset;

import android.graphics.drawable.Drawable;

import com.google.common.base.Preconditions;

public class DefaultValuePackageAssetServiceDecorator implements PackageAssetService {
    private final PackageAssetService packageAssetService;
    private final Drawable defaultIcon;

    public DefaultValuePackageAssetServiceDecorator(final PackageAssetService packageAssetService,
                                                    final Drawable defaultIcon) {
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.defaultIcon = Preconditions.checkNotNull(defaultIcon);
    }

    @Override
    public PackageAssets getPackageAssets(final String packageName) {
        PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
        if (packageAssets != null) {
            return packageAssets;
        }
        return new PackageAssets(packageName, packageName, defaultIcon);
    }
}
