package com.mysticwind.disabledappmanager.domain.app.impl;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationFilter;
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
        return getPackages(new ApplicationFilter());
    }

    @Override
    public Set<AppInfo> getPackages(final ApplicationFilter applicationFilter) {
        Preconditions.checkNotNull(applicationFilter);

        return stream(getApplicationInfos(applicationFilter))
                .map(appInfo -> convert(appInfo))
                .collect(Collectors.toSet());
    }

    private Set<ApplicationInfo> getApplicationInfos(final ApplicationFilter applicationFilter) {
        return stream(packageManager.getInstalledApplications(PackageManager.GET_META_DATA))
                .filter(applicationInfo -> shouldIncludeApplicationInfo(applicationInfo, applicationFilter))
                .collect(Collectors.toSet());
    }

    private boolean shouldIncludeApplicationInfo(final ApplicationInfo applicationInfo,
                                                 final ApplicationFilter applicationFilter) {
        if (!applicationFilter.isIncludeSystemApp() && isSystemApp(applicationInfo)) {
            return false;
        }
        return true;
    }

    private boolean isSystemApp(final ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & (ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) > 0;
    }

    @Override
    public List<AppInfo> getOrderedPackages(final ApplicationOrderingMethod orderingMethod) {
        return getOrderedPackages(new ApplicationFilter(), orderingMethod);
    }

    @Override
    public List<AppInfo> getOrderedPackages(final ApplicationFilter applicationFilter,
                                            final ApplicationOrderingMethod orderingMethod) {
        Preconditions.checkNotNull(applicationFilter);
        Preconditions.checkNotNull(orderingMethod);

        switch (orderingMethod) {
            case PACKAGE_NAME:
                return stream(getApplicationInfos(applicationFilter))
                        .map(appInfo -> convert(appInfo))
                        .sorted((appInfo1, appInfo2) -> appInfo1.getPackageName().compareTo(appInfo2.getPackageName()))
                        .collect(Collectors.toList());
            case APPLICATION_LABEL:
                return stream(getApplicationInfos(applicationFilter))
                        .map(applicationInfo -> new ApplicationLabelIncludedAppInfo(
                                packageAssetService.getPackageAssets(applicationInfo.packageName).getAppName(),
                                applicationInfo))
                        .sorted((appInfo1, appInfo2) -> appInfo1.getApplicationLabel().compareTo(appInfo2.getApplicationLabel()))
                        .map(applicationInfo -> convert(applicationInfo.applicationInfo))
                        .collect(Collectors.toList());
            default:
                throw new IllegalArgumentException("Unsupported ordering method: " + orderingMethod);
        }
    }

    private AppInfo convert(ApplicationInfo appInfo) {
        return new AppInfo(appInfo.packageName, appInfo.enabled, isSystemApp(appInfo));
    }

    @Value
    private class ApplicationLabelIncludedAppInfo {
        private final String applicationLabel;
        private final ApplicationInfo applicationInfo;
    }
}
