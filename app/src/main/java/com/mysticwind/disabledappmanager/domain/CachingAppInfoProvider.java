package com.mysticwind.disabledappmanager.domain;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public enum CachingAppInfoProvider implements AppIconProvider, AppNameProvider {
    INSTANCE;

    private static final int THREAD_POOL_SIZE = 5;
    private boolean isInitialized = false;
    private AppIconProvider appIconProvider;
    private AppNameProvider appNameProvider;
    private PackageManager packageManager;
    private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    private Map<String, Drawable> packageNameToIconMap = new ConcurrentHashMap<>();
    private Map<String, String> packageNameToAppNameMap = new ConcurrentHashMap<>();

    public synchronized CachingAppInfoProvider init(
            AppIconProvider appIconProvider,
            AppNameProvider appNameProvider,
            PackageManager packageManager) {
        if (isInitialized) {
            return this;
        }
        this.appIconProvider = appIconProvider;
        this.appNameProvider = appNameProvider;
        this.packageManager = packageManager;
        isInitialized = true;
        preloadAppInfos();
        return this;
    }

    private void preloadAppInfos() {
        List<ApplicationInfo> applicationInfoList
                = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);
        for (final ApplicationInfo appInfo : applicationInfoList) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    String packageName = appInfo.packageName;

                    if (!packageNameToAppNameMap.containsKey(packageName)) {
                        String appName = appNameProvider.getAppName(packageName);
                        packageNameToAppNameMap.put(packageName, appName);
                    }

                    if (!packageNameToIconMap.containsKey(packageName)) {
                        Drawable icon = appIconProvider.getAppIcon(packageName);
                        packageNameToIconMap.put(packageName, icon);
                    }
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

    @Override
    public String getAppName(String packageName) {
        String appName = packageNameToAppNameMap.get(packageName);
        if (appName == null) {
            appName = appNameProvider.getAppName(packageName);
            packageNameToAppNameMap.put(packageName, appName);
        }
        return appName;
    }
}
