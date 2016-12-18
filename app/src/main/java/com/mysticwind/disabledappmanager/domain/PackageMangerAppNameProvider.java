package com.mysticwind.disabledappmanager.domain;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageMangerAppNameProvider implements AppNameProvider {

    private final PackageManager packageManager;

    public PackageMangerAppNameProvider(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public String getAppName(String packageName) {
        try {
            ApplicationInfo appInfo
                    = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            return appInfo.loadLabel(packageManager).toString();
        } catch (PackageManager.NameNotFoundException e) {
            String errorMessage = "Failed to obtain application name for package: " + packageName;
            log.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}
