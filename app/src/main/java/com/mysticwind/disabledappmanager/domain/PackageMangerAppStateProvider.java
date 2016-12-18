package com.mysticwind.disabledappmanager.domain;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageMangerAppStateProvider implements AppStateProvider {

    private final PackageManager packageManager;

    public PackageMangerAppStateProvider(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public boolean isPackageEnabled(String packageName) {
        try {
            ApplicationInfo appInfo =
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return appInfo.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            String errorMessage = "Failed to obtain application info for package: " + packageName;
            log.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}
