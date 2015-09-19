package com.mysticwind.disabledappmanager.domain;

import android.content.pm.PackageManager;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.Set;

public class PackageManagerDisabledPackageListProvider extends PackageMangerPackageListProviderBase {
    public PackageManagerDisabledPackageListProvider(PackageManager packageManager) {
        super(packageManager);
    }

    @Override
    public Set<AppInfo> getPackages() {
        return getPackagesWithStatus(false);
    }
}
