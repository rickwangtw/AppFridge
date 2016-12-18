package com.mysticwind.disabledappmanager.domain;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class PackageMangerPackageListProviderBase extends AbstractPackageListProvider {

    private final PackageManager packageManager;

    public PackageMangerPackageListProviderBase(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    protected Set<AppInfo> getAllPackages() {
        List<ApplicationInfo> appInfoList
                = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<AppInfo> appInfoSet = new HashSet<>();
        for (ApplicationInfo appInfo : appInfoList) {
            appInfoSet.add(new AppInfo(appInfo));
        }
        return appInfoSet;
    }

    protected Set<AppInfo> getPackagesWithStatus(boolean enabled) {
        Set<AppInfo> selectedPackages = new HashSet<>();
        for (AppInfo appInfo : getAllPackages()) {
            if (! (enabled ^ appInfo.isEnabled()) ) {
                selectedPackages.add(appInfo);
            }
        }
        return selectedPackages;
    }
}
