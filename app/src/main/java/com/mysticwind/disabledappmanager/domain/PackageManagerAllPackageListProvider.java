package com.mysticwind.disabledappmanager.domain;

import android.content.pm.PackageManager;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.Set;

public class PackageManagerAllPackageListProvider extends PackageMangerPackageListProviderBase {
    public PackageManagerAllPackageListProvider(PackageManager packageManager) {
        super(packageManager);
    }

    @Override
    public Set<AppInfo> getPackages() {
        return getAllPackages();
    }
}
