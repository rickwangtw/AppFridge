package com.mysticwind.disabledappmanager.domain;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.gmr.acacia.AcaciaService;
import com.gmr.acacia.ServiceAware;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.thread.RequestStackThreadPoolExecutor;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.ui.common.Action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class PackageManagerPackageAssetService implements PackageAssetService, ServiceAware<AcaciaService> {
    private final static String TAG = "PMPackageAssetService";
    private static final int THREAD_POOL_SIZE = 5;
    private static final int MAX_POOL_SIZE = 1024;
    private final static Map<String, Drawable> PACKAGE_NAME_TO_ICON_MAP = new ConcurrentHashMap<>();
    private final static Map<String, String> PACKAGE_NAME_TO_APP_NAME_MAP = new ConcurrentHashMap<>();
    private final static ExecutorService THREAD_POOL_EXECUTOR =
            new RequestStackThreadPoolExecutor(THREAD_POOL_SIZE, MAX_POOL_SIZE, 5, TimeUnit.MINUTES);

    private AppIconProvider appIconProvider;
    private AppNameProvider appNameProvider;
    private Drawable defaultIcon;

    @Override
    public void setAndroidService(AcaciaService androidService) {
        appIconProvider = new PackageMangerAppIconProvider(androidService.getPackageManager());
        appNameProvider = new PackageMangerAppNameProvider(androidService.getPackageManager());
        defaultIcon = androidService.getResources().getDrawable(R.drawable.stub);

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
        new PackageAssetLoaderAsyncTask(packageName, appIconProvider, appNameProvider)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private static class PackageAssetLoaderAsyncTask extends AsyncTask<Void, Void, Boolean> {
        private final String packageName;
        private final AppIconProvider appIconProvider;
        private final AppNameProvider appNameProvider;

        PackageAssetLoaderAsyncTask(String packageName,
                                    AppIconProvider appIconProvider,
                                    AppNameProvider appNameProvider) {
            this.packageName = packageName;
            this.appIconProvider = appIconProvider;
            this.appNameProvider = appNameProvider;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            boolean assetUpdated = false;
            if (!PACKAGE_NAME_TO_APP_NAME_MAP.containsKey(packageName)) {
                String appName = appNameProvider.getAppName(packageName);
                PACKAGE_NAME_TO_APP_NAME_MAP.put(packageName, appName);
                assetUpdated = true;
            }
            if (!PACKAGE_NAME_TO_ICON_MAP.containsKey(packageName)) {
                Drawable icon = appIconProvider.getAppIcon(packageName);
                PACKAGE_NAME_TO_ICON_MAP.put(packageName, icon);
                assetUpdated = true;
            }
            return assetUpdated;
        }

        @Override
        protected void onPostExecute(Boolean assetUpdated) {
            super.onPostExecute(assetUpdated);

            if (assetUpdated) {
                EventBus.getDefault().post(Action.PACKAGE_ASSET_UPDATED);
            }
        }
    }
}
