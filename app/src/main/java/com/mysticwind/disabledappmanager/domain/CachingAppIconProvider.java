package com.mysticwind.disabledappmanager.domain;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum CachingAppIconProvider implements AppIconProvider {
    INSTANCE;

    private static final int THREAD_POOL_SIZE = 5;
    private boolean isInitialized = false;
    private AppIconProvider appIconProvider;
    private PackageManager packageManager;
    private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private Map<String, Drawable> packageNameToIconMap = new ConcurrentHashMap<>();

    public synchronized CachingAppIconProvider init(AppIconProvider appIconProvider, PackageManager packageManager) {
        if (isInitialized) {
            return this;
        }
        this.appIconProvider = appIconProvider;
        this.packageManager = packageManager;
        isInitialized = true;
        preloadIcons();
        return this;
    }

    private void preloadIcons() {
        List<ApplicationInfo> applicationInfoList
                = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (final ApplicationInfo appInfo : applicationInfoList) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    String packageName = appInfo.packageName;
                    Drawable icon = appIconProvider.getAppIcon(packageName);
                    packageNameToIconMap.put(packageName, icon);
                }
            });
        }
    }

    @Override
    public Drawable getAppIcon(String packageName) {
        Drawable icon = packageNameToIconMap.get(packageName);
        if (icon == null) {
            icon = appIconProvider.getAppIcon(packageName);
            packageNameToIconMap.put(packageName, icon);
        }
        return icon;
    }
}
