package com.mysticwind.disabledappmanager.domain;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.gmr.acacia.AcaciaService;
import com.gmr.acacia.ServiceAware;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PackageManagerPackageAssetService implements PackageAssetService, ServiceAware<AcaciaService> {
    private final static Map<String, Drawable> PACKAGE_NAME_TO_ICON_MAP = new ConcurrentHashMap<>();
    private final static Map<String, String> PACKAGE_NAME_TO_APP_NAME_MAP = new ConcurrentHashMap<>();

    private AppIconProvider appIconProvider;
    private AppNameProvider appNameProvider;
    private Drawable defaultIcon;

    @Override
    public void setAndroidService(AcaciaService androidService) {
        appIconProvider = new PackageMangerAppIconProvider(androidService.getPackageManager());
        appNameProvider = new PackageMangerAppNameProvider(androidService.getPackageManager());
        defaultIcon = androidService.getResources().getDrawable(R.drawable.stub);

        new AllPackageAssetLoaderAsyncTask(
                new PackageManagerAllPackageListProvider(androidService.getPackageManager()),
                appIconProvider,
                appNameProvider).execute();
    }

    @Override
    public Drawable getAppIcon(String packageName) {
        Drawable icon = PACKAGE_NAME_TO_ICON_MAP.get(packageName);
        if (icon == null) {
            new SinglePackageAssetLoaderAsyncTask(packageName, appIconProvider, appNameProvider).execute();
            return defaultIcon;
        }
        return icon;
    }

    @Override
    public String getAppName(String packageName) {
        String applicationName =  PACKAGE_NAME_TO_APP_NAME_MAP.get(packageName);
        if (applicationName == null) {
            new SinglePackageAssetLoaderAsyncTask(packageName, appIconProvider, appNameProvider).execute();
            return packageName;
        }
        return applicationName;
    }

    private static class SinglePackageAssetLoaderAsyncTask extends AsyncTask<Void, Void, Void> {
        private final String packageName;
        private final AppIconProvider appIconProvider;
        private final AppNameProvider appNameProvider;

        SinglePackageAssetLoaderAsyncTask(String packageName,
                                          AppIconProvider appIconProvider, AppNameProvider appNameProvider) {
            this.packageName = packageName;
            this.appIconProvider = appIconProvider;
            this.appNameProvider = appNameProvider;
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (!PACKAGE_NAME_TO_APP_NAME_MAP.containsKey(packageName)) {
                String appName = appNameProvider.getAppName(packageName);
                PACKAGE_NAME_TO_APP_NAME_MAP.put(packageName, appName);
            }
            if (!PACKAGE_NAME_TO_ICON_MAP.containsKey(packageName)) {
                Drawable icon = appIconProvider.getAppIcon(packageName);
                PACKAGE_NAME_TO_ICON_MAP.put(packageName, icon);
            }
            return null;
        }
    }

    private static class AllPackageAssetLoaderAsyncTask extends AsyncTask<Void, Void, Void> {
        private static final String TAG = "AssetLoaderAsyncTask";
        private static final int THREAD_POOL_SIZE = 5;
        private ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        private final PackageListProvider packageListProvider;
        private final AppIconProvider appIconProvider;
        private final AppNameProvider appNameProvider;

        AllPackageAssetLoaderAsyncTask(PackageListProvider packageListProvider,
                                       AppIconProvider appIconProvider,
                                       AppNameProvider appNameProvider) {
            this.packageListProvider = packageListProvider;
            this.appIconProvider = appIconProvider;
            this.appNameProvider = appNameProvider;
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<AppInfo> packageList = packageListProvider.getOrderedPackages();
            for (final AppInfo appInfo : packageList) {
                threadPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        String packageName = appInfo.getPackageName();

                        if (!PACKAGE_NAME_TO_APP_NAME_MAP.containsKey(packageName)) {
                            String appName = appNameProvider.getAppName(packageName);
                            PACKAGE_NAME_TO_APP_NAME_MAP.put(packageName, appName);
                        }

                        if (!PACKAGE_NAME_TO_ICON_MAP.containsKey(packageName)) {
                            Drawable icon = appIconProvider.getAppIcon(packageName);
                            PACKAGE_NAME_TO_ICON_MAP.put(packageName, icon);
                        }
                    }
                });
            }
            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.w(TAG, "Failed to wait for package asset to load", e);
            }
            threadPool.shutdown();
            return null;
        }
    }
}
