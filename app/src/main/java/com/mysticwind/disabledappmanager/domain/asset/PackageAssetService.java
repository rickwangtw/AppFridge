package com.mysticwind.disabledappmanager.domain.asset;

import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;

public interface PackageAssetService extends AppIconProvider, AppNameProvider {
    PackageAssets getPackageAssets(String packageName);
}
