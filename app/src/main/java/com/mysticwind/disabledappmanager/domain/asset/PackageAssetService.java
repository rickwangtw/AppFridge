package com.mysticwind.disabledappmanager.domain.asset;

import com.gmr.acacia.Service;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;

@Service(value=PackageManagerPackageAssetService.class, useWorkerThread=false)
    public interface PackageAssetService extends AppIconProvider, AppNameProvider {
}
