package com.mysticwind.disabledappmanager.domain;

import android.content.Intent;
import android.content.pm.PackageManager;

public class PackageManagerAppLauncher implements AppLauncher {
    private final PackageManager packageManager;

    public PackageManagerAppLauncher(PackageManager packageManager) {
        this.packageManager = packageManager;
    }

    @Override
    public Intent getLaunchIntentForPackage(String packageName) {
        return packageManager.getLaunchIntentForPackage(packageName);
    }
}
