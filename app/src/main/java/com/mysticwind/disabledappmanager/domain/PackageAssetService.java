package com.mysticwind.disabledappmanager.domain;

import com.gmr.acacia.Service;

@Service(value=PackageManagerPackageAssetService.class, useWorkerThread=false)
    public interface PackageAssetService extends AppIconProvider, AppNameProvider {
}
