package com.mysticwind.disabledappmanager.domain;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import com.gmr.acacia.AcaciaService;
import com.gmr.acacia.ServiceAware;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.ui.common.Action;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;

public class PackageManagerPackageAssetService implements PackageAssetService, ServiceAware<AcaciaService> {
    private final static String TAG = "PMPackageAssetService";
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
            return defaultIcon;
        }
        return icon;
    }

    @Override
    public String getAppName(String packageName) {
        String applicationName =  PACKAGE_NAME_TO_APP_NAME_MAP.get(packageName);
        if (applicationName == null) {
            return packageName;
        }
        return applicationName;
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
                        if (assetUpdated) {
                            EventBus.getDefault().post(Action.PACKAGE_ASSET_UPDATED);
                        }
                    }
                });
            }
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Log.w(TAG, "Failed to wait for package asset to load", e);
            }
            Log.d(TAG, "Finished caching all packages");
            return null;
        }
    }
}
