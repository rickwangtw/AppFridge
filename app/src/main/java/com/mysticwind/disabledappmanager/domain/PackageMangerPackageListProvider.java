package com.mysticwind.disabledappmanager.domain;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PackageMangerPackageListProvider implements PackageListProvider {
    private static final String TAG = "PMPackageListProvider";

    private final PackageManager packageManager;

    public PackageMangerPackageListProvider(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public Set<AppInfo> getAllPackages() {
        List<ApplicationInfo> appInfoList
                = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        Set<AppInfo> appInfoSet = new HashSet<>();
        for (ApplicationInfo appInfo : appInfoList) {
            appInfoSet.add(new AppInfo(appInfo));
        }
        return appInfoSet;
    }

    @Override
    public List<AppInfo> getOrderedAllPackages() {
        return packageListOrderedByPackageName(getAllPackages());
    }

    private List<AppInfo> packageListOrderedByPackageName(Collection<AppInfo> packages) {
        List<AppInfo> orderedPackageList = new ArrayList<>(packages);
        Collections.sort(orderedPackageList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                return lhs.getPackageName().compareTo(rhs.getPackageName());
            }
        });
        return orderedPackageList;
    }

    @Override
    public Set<AppInfo> getEnabledPackages() {
        return getPackagesWithStatus(true);
    }

    private Set<AppInfo> getPackagesWithStatus(boolean enabled) {
        Set<AppInfo> selectedPackages = new HashSet<>();
        for (AppInfo appInfo : getAllPackages()) {
            if (enabled ^ appInfo.isEnabled()) {
                selectedPackages.add(appInfo);
            }
        }
        return selectedPackages;
    }

    @Override
    public List<AppInfo> getOrderedEnabledPackages() {
        return packageListOrderedByPackageName(getEnabledPackages());
    }

    @Override
    public Set<AppInfo> getDisabledPackages() {
        return getPackagesWithStatus(false);
    }

    @Override
    public List<AppInfo> getOrderedDisabledPackages() {
        return packageListOrderedByPackageName(getDisabledPackages());
    }
}
