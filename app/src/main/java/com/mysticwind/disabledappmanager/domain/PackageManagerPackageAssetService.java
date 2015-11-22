package com.mysticwind.disabledappmanager.domain;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.gmr.acacia.AcaciaService;
import com.gmr.acacia.ServiceAware;
import com.google.common.collect.ImmutableSet;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.common.thread.RequestStackThreadPoolExecutor;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AssetType;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageManagerPackageAssetService implements PackageAssetService, ServiceAware<AcaciaService> {
    private static final int THREAD_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 1024;
    private final static Map<String, Drawable> PACKAGE_NAME_TO_ICON_MAP = new ConcurrentHashMap<>();
    private final static Map<String, String> PACKAGE_NAME_TO_APP_NAME_MAP = new ConcurrentHashMap<>();
    private final static ExecutorService THREAD_POOL_EXECUTOR =
            new RequestStackThreadPoolExecutor(THREAD_POOL_SIZE, MAX_POOL_SIZE, 5, TimeUnit.MINUTES);

    private AppIconProvider appIconProvider;
    private AppNameProvider appNameProvider;
    private Drawable defaultIcon;
    private AppAssetUpdateEventManager appAssetUpdateEventManager;

    @Override
    public void setAndroidService(AcaciaService androidService) {
        appIconProvider = new PackageMangerAppIconProvider(androidService.getPackageManager());
        appNameProvider = new PackageMangerAppNameProvider(androidService.getPackageManager());
        defaultIcon = androidService.getResources().getDrawable(R.drawable.stub);
        appAssetUpdateEventManager =
                ApplicationHelper.from(androidService.getApplication()).appAssetUpdateEventManager();

        preloadAssetForAllPackages(androidService.getPackageManager());
    }

    private void preloadAssetForAllPackages(PackageManager packageManager) {
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
        new PackageAssetLoaderAsyncTask(
                packageName, appIconProvider, appNameProvider, appAssetUpdateEventManager)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private static class PackageAssetLoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final String packageName;
        private final AppIconProvider appIconProvider;
        private final AppNameProvider appNameProvider;
        private final AppAssetUpdateEventManager appAssetUpdateEventManager;
        private boolean appNameUpdated = false;
        private boolean appIconUpdated = false;

        PackageAssetLoaderAsyncTask(String packageName,
                                    AppIconProvider appIconProvider,
                                    AppNameProvider appNameProvider,
                                    AppAssetUpdateEventManager appAssetUpdateEventManager) {
            this.packageName = packageName;
            this.appIconProvider = appIconProvider;
            this.appNameProvider = appNameProvider;
            this.appAssetUpdateEventManager = appAssetUpdateEventManager;
            if (appAssetUpdateEventManager == null) {
                log.warn("App asset update event manager not configured!");
            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            if (!PACKAGE_NAME_TO_APP_NAME_MAP.containsKey(packageName)) {
                String appName = appNameProvider.getAppName(packageName);
                PACKAGE_NAME_TO_APP_NAME_MAP.put(packageName, appName);
                appNameUpdated = true;
            }
            if (!PACKAGE_NAME_TO_ICON_MAP.containsKey(packageName)) {
                Drawable icon = appIconProvider.getAppIcon(packageName);
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
                updatedAssetTypesBuilder.add(AssetType.PACKAGE_NAME);
            }
            if (appIconUpdated) {
                updatedAssetTypesBuilder.add(AssetType.ICON);
            }

            appAssetUpdateEventManager.publishUpdate(packageName, updatedAssetTypesBuilder.build());
        }
    }
}
