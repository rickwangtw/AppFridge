package com.mysticwind.disabledappmanager.domain.asset;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.domain.asset.dao.CachedPackageAssetsDAO;

import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class DatabaseCachedPackageAssetServiceDecorator implements PackageAssetService {

    private static final int THREAD_POOL_SIZE = 1;
    private static final int MAX_POOL_SIZE = 10_000;
    private static final long KEEP_ALIVE_IN_MINUTES = Long.MAX_VALUE;

    private final ExecutorService threadPool = new ThreadPoolExecutor(THREAD_POOL_SIZE, MAX_POOL_SIZE,
            KEEP_ALIVE_IN_MINUTES, TimeUnit.MINUTES, new ArrayBlockingQueue<>(MAX_POOL_SIZE));

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

        threadPool.submit(() ->
                cachedPackageAssetsDAO.createOrUpdate(packageAssets.getPackageName(),
                        packageAssets.getAppName(), packageAssets.getIconDrawable())
        );
    }
}
