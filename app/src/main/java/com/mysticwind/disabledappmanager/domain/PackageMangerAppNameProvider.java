package com.mysticwind.disabledappmanager.domain;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class PackageMangerAppNameProvider implements AppNameProvider {
    private static final String TAG = "PMAppNameProvider";

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
            Log.e(TAG, errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}