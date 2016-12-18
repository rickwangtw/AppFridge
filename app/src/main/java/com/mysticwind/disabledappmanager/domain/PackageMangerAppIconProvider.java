package com.mysticwind.disabledappmanager.domain;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageMangerAppIconProvider implements AppIconProvider {

    private final PackageManager packageManager;

    public PackageMangerAppIconProvider(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public Drawable getAppIcon(String packageName) {
        try {
            return packageManager.getApplicationIcon(packageName).getCurrent();
        } catch (PackageManager.NameNotFoundException e) {
            String errorMessage = "Failed to obtain application icon for package: " + packageName;
            log.error(errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}
