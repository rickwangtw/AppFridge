package com.mysticwind.disabledappmanager.domain.app.impl;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.app.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Set;

import java8.util.stream.Collectors;
import lombok.Value;

import static java8.util.stream.StreamSupport.stream;

public class PackageManagerPackageListProvider implements PackageListProvider {

    private final PackageManager packageManager;
    private final PackageAssetService packageAssetService;

    public PackageManagerPackageListProvider(final PackageManager packageManager,
                                             final PackageAssetService packageAssetService) {
        this.packageManager = Preconditions.checkNotNull(packageManager);
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
    }

    @Override
    public Set<AppInfo> getPackages() {
        return stream(getAllPackages())
                .map(appInfo -> new AppInfo(appInfo))
                .collect(Collectors.toSet());
    }

    private Set<ApplicationInfo> getAllPackages() {
        return stream(packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
                .collect(Collectors.toSet());
    }

    @Override
    public List<AppInfo> getOrderedPackages(final ApplicationOrderingMethod orderingMethod) {
        Preconditions.checkNotNull(orderingMethod);

        switch (orderingMethod) {
            case PACKAGE_NAME:
                return stream(getAllPackages())
                        .map(appInfo -> new AppInfo(appInfo))
                        .sorted((appInfo1, appInfo2) -> appInfo1.getPackageName().compareTo(appInfo2.getPackageName()))
                        .collect(Collectors.toList());
            case APPLICATION_LABEL:
                return stream(getAllPackages())
                        .map(applicationInfo -> new ApplicationLabelIncludedAppInfo(
                                packageAssetService.getPackageAssets(applicationInfo.packageName).getAppName(),
                                applicationInfo))
                        .sorted((appInfo1, appInfo2) -> appInfo1.getApplicationLabel().compareTo(appInfo2.getApplicationLabel()))
                        .map(applicationInfo -> new AppInfo(applicationInfo.getApplicationInfo()))
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unsupported ordering method: " + orderingMethod);
        }
    }

    @Value
    private class ApplicationLabelIncludedAppInfo {
        private final String applicationLabel;
        private final ApplicationInfo applicationInfo;
    }
}
