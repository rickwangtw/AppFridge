package com.mysticwind.disabledappmanager.domain.model;

import android.content.pm.ApplicationInfo;

public class AppInfo {
    private final String packageName;
    private final boolean enabled;

    public AppInfo(ApplicationInfo appInfo) {
        this.packageName = appInfo.packageName;
        this.enabled = appInfo.enabled;
    }

    public String getPackageName() {
        return packageName;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
