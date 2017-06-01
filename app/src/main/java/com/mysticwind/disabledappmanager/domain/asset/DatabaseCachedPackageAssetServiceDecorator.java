package com.mysticwind.disabledappmanager.domain.asset;

import android.os.AsyncTask;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.domain.asset.dao.CachedPackageAssetsDAO;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class DatabaseCachedPackageAssetServiceDecorator implements PackageAssetService {
    private final static ExecutorService THREAD_POOL_EXECUTOR = Executors.newSingleThreadExecutor();

    private final PackageAssetService packageAssetService;
    private final CachedPackageAssetsDAO cachedPackageAssetsDAO;
    private final Map<String, PackageAssets> packageNameToPackageAssetsMap = new ConcurrentHashMap<>();

    public DatabaseCachedPackageAssetServiceDecorator(final PackageAssetService packageAssetService,
                                                      final CachedPackageAssetsDAO cachedPackageAssetsDAO) {
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.cachedPackageAssetsDAO = Preconditions.checkNotNull(cachedPackageAssetsDAO);
    }

    @Override
    public PackageAssets getPackageAssets(String packageName) {
        if (packageNameToPackageAssetsMap.isEmpty()) {
            loadCache();
        }
        PackageAssets packageAssets = packageNameToPackageAssetsMap.get(packageName);
        if (packageAssets != null) {
            return packageAssets;
        }
        packageAssets = packageAssetService.getPackageAssets(packageName);
        updateCacheAndDatabaseEntry(packageName, packageAssets);
        return packageAssets;
    }

    private void loadCache() {
        stream(cachedPackageAssetsDAO.getAllPackageAssets()).forEach(packageAssets ->
                packageNameToPackageAssetsMap.put(packageAssets.getPackageName(), packageAssets)
        );
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
