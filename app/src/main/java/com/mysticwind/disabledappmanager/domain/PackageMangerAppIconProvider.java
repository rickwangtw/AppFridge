package com.mysticwind.disabledappmanager.domain;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class PackageMangerAppIconProvider implements AppIconProvider {
    private static final String TAG = "PMAppIconProvider";

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
            Log.e(TAG, errorMessage, e);
            throw new IllegalArgumentException(errorMessage, e);
        }
    }
}
