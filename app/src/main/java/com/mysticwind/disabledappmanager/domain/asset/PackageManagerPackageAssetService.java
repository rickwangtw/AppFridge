package com.mysticwind.disabledappmanager.domain.asset;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.google.common.collect.ImmutableSet;
import com.mysticwind.disabledappmanager.common.thread.RequestStackThreadPoolExecutor;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageManagerPackageAssetService implements PackageAssetService {
    private static final int THREAD_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 1024;
    private final static Map<String, Drawable> PACKAGE_NAME_TO_ICON_MAP = new ConcurrentHashMap<>();
    private final static Map<String, String> PACKAGE_NAME_TO_APP_NAME_MAP = new ConcurrentHashMap<>();
    private final static ExecutorService THREAD_POOL_EXECUTOR =
            new RequestStackThreadPoolExecutor(THREAD_POOL_SIZE, MAX_POOL_SIZE, 5, TimeUnit.MINUTES);

    private final PackageManager packageManager;
    private final AppAssetUpdateEventManager appAssetUpdateEventManager;
    private final Drawable defaultIcon;

    public PackageManagerPackageAssetService(
            PackageManager packageManager,
            AppAssetUpdateEventManager appAssetUpdateEventManager,
            Drawable defaultIcon) {
        this.packageManager = packageManager;
        this.appAssetUpdateEventManager = appAssetUpdateEventManager;
        this.defaultIcon = defaultIcon;

        preloadAssetForAllPackages();
    }

    private void preloadAssetForAllPackages() {
        List<AppInfo> packageList =
                new PackageManagerAllPackageListProvider(packageManager).getOrderedPackages();
        for (final AppInfo appInfo : packageList) {
            runPackageAssetLoaderAsyncTask(appInfo.getPackageName());
        }
    }

    @Override
    public Drawable getAppIcon(String packageName) {
        Drawable icon = PACKAGE_NAME_TO_ICON_MAP.get(packageName);
        if (icon == null) {
            runPackageAssetLoaderAsyncTask(packageName);
            return defaultIcon;
        }
        return icon;
    }

    @Override
    public String getAppName(String packageName) {
        String applicationName = PACKAGE_NAME_TO_APP_NAME_MAP.get(packageName);
        if (applicationName == null) {
            runPackageAssetLoaderAsyncTask(packageName);
            return packageName;
        }
        return applicationName;
    }

    private void runPackageAssetLoaderAsyncTask(String packageName) {
        new PackageAssetLoaderAsyncTask(packageName, packageManager, appAssetUpdateEventManager)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private static class PackageAssetLoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final String packageName;
        private final PackageManager packageManager;
        private final AppAssetUpdateEventManager appAssetUpdateEventManager;
        private boolean appNameUpdated = false;
        private boolean appIconUpdated = false;

        PackageAssetLoaderAsyncTask(String packageName,
                                    PackageManager packageManager,
                                    AppAssetUpdateEventManager appAssetUpdateEventManager) {
            this.packageName = packageName;
            this.packageManager = packageManager;
            this.appAssetUpdateEventManager = appAssetUpdateEventManager;
            if (appAssetUpdateEventManager == null) {
                log.warn("App asset update event manager not configured!");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            ApplicationInfo appInfo;
            try {
                appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            } catch (PackageManager.NameNotFoundException e) {
                log.warn("Failed to get application info for package: " + packageName);
                return false;
            }

            if (!PACKAGE_NAME_TO_APP_NAME_MAP.containsKey(packageName)) {
                String appName = appInfo.loadLabel(packageManager).toString();
                PACKAGE_NAME_TO_APP_NAME_MAP.put(packageName, appName);
                appNameUpdated = true;
            }
            if (!PACKAGE_NAME_TO_ICON_MAP.containsKey(packageName)) {
                Drawable icon = appInfo.loadIcon(packageManager);
                PACKAGE_NAME_TO_ICON_MAP.put(packageName, icon);
                appIconUpdated = true;
            }
            return appNameUpdated || appIconUpdated;
        }

        @Override
        protected void onPostExecute(Boolean assetUpdated) {
            super.onPostExecute(assetUpdated);
            if (!assetUpdated) {
                return;
            }
            if (appAssetUpdateEventManager == null) {
                return;
            }

            ImmutableSet.Builder<AssetType> updatedAssetTypesBuilder = new ImmutableSet.Builder<>();
            if (appNameUpdated) {
                updatedAssetTypesBuilder.add(AssetType.APP_NAME);
            }
            if (appIconUpdated) {
                updatedAssetTypesBuilder.add(AssetType.ICON);
            }

            appAssetUpdateEventManager.publishUpdate(packageName, updatedAssetTypesBuilder.build());
        }
    }
}
