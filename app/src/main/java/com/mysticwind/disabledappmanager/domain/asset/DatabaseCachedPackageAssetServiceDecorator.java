package com.mysticwind.disabledappmanager.domain.asset;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.mysticwind.disabledappmanager.domain.asset.dao.CachedPackageAssetsDAO;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DatabaseCachedPackageAssetServiceDecorator implements PackageAssetService {
    private final static ExecutorService THREAD_POOL_EXECUTOR = Executors.newSingleThreadExecutor();

    private final PackageAssetService packageAssetService;
    private final CachedPackageAssetsDAO cachedPackageAssetsDAO;
    private final Map<String, PackageAssets> packageNameToPackageAssetsMap = new ConcurrentHashMap<>();

    public DatabaseCachedPackageAssetServiceDecorator(
            PackageAssetService packageAssetService, CachedPackageAssetsDAO cachedPackageAssetsDAO) {
        this.packageAssetService = packageAssetService;
        this.cachedPackageAssetsDAO = cachedPackageAssetsDAO;

        new DatabaseEntriesLoaderAsyncTask(cachedPackageAssetsDAO, packageNameToPackageAssetsMap)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    @Override
    public Drawable getAppIcon(String packageName) {
        return getPackageAssets(packageName).getIconDrawable();
    }

    @Override
    public String getAppName(String packageName) {
        return getPackageAssets(packageName).getAppName();
    }

    @Override
    public PackageAssets getPackageAssets(String packageName) {
        PackageAssets packageAssets = packageNameToPackageAssetsMap.get(packageName);
        if (packageAssets != null) {
            return packageAssets;
        }
        packageAssets = packageAssetService.getPackageAssets(packageName);
        updateCacheAndDatabaseEntry(packageName, packageAssets);
        return packageAssets;
    }

    private void updateCacheAndDatabaseEntry(String packageName, PackageAssets packageAssets) {
        if (packageAssets == null ||
                packageAssets.getAppName() == null || packageAssets.getIconDrawable() == null) {
            return;
        }
        packageNameToPackageAssetsMap.put(packageName, packageAssets);
        new DatabaseEntryUpdateAsyncTask(cachedPackageAssetsDAO, packageAssets)
                .executeOnExecutor(THREAD_POOL_EXECUTOR);
    }

    private static class DatabaseEntriesLoaderAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CachedPackageAssetsDAO dataAccessor;
        private final Map<String, PackageAssets> packageNameToPackageAssetsMap;

        public DatabaseEntriesLoaderAsyncTask(
                CachedPackageAssetsDAO dataAccessor,
                Map<String, PackageAssets> packageNameToPackageAssetsMap) {
            this.dataAccessor = dataAccessor;
            this.packageNameToPackageAssetsMap = packageNameToPackageAssetsMap;
        }

        @Override
        protected Void doInBackground(Void... params) {
            List<PackageAssets> packageAssetsList = dataAccessor.getAllPackageAssets();
            for (PackageAssets packageAssets : packageAssetsList) {
                packageNameToPackageAssetsMap.put(packageAssets.getPackageName(), packageAssets);
            }
            return null;
        }
    }

    private static class DatabaseEntryUpdateAsyncTask extends AsyncTask<Void, Void, Void> {
        private final CachedPackageAssetsDAO dataAccessor;
        private final PackageAssets packageAssets;

        public DatabaseEntryUpdateAsyncTask(CachedPackageAssetsDAO dataAccessor, PackageAssets packageAssets) {
            this.dataAccessor = dataAccessor;
            this.packageAssets = packageAssets;
        }

        @Override
        protected Void doInBackground(Void... params) {
            dataAccessor.createOrUpdate(packageAssets.getPackageName(),
                    packageAssets.getAppName(), packageAssets.getIconDrawable());
            return null;
        }
    }
}
