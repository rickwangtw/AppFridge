package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.os.AsyncTask;
import android.os.Bundle;

import com.google.common.collect.ImmutableSet;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationFilter;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.asset.AssetType;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import org.androidannotations.annotations.EActivity;

import java.util.Collection;
import java.util.List;

import java8.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
@EActivity
public class FirstLaunchOptimizedPackageStatePerspective extends PackageStatePerspectiveBase {

    private boolean packageAssetLoadingCompleted = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadingDialog.show();

        setupView(true);

        loadingDialog.dismiss();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (!packageAssetLoadingCompleted) {
            packageAssetLoadingCompleted = true;
            packageAssetLoadingAsyncTask().execute();
        }
    }

    private AsyncTask<Void, Void, Void> packageAssetLoadingAsyncTask() {
        return new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                List<AppInfo> appInfoList = packageListProvider.getOrderedPackages(
                        ApplicationFilter.builder()
                                .includeSystemApp(true)
                                .build(),
                        ApplicationOrderingMethod.PACKAGE_NAME);

                // load assets for non-system apps
                loadPackageAssetsAndPublishAssetChangeEvent(appInfoList, appInfo -> !appInfo.isSystemApp());

                // load assets for system apps
                loadPackageAssetsAndPublishAssetChangeEvent(appInfoList, appInfo -> appInfo.isSystemApp());
                return null;
            }
        };
    }

    private void loadPackageAssetsAndPublishAssetChangeEvent(final Collection<AppInfo> appInfos,
                                                             final Predicate<AppInfo> appInfoPredicate) {
        stream(appInfos)
                .filter(appInfoPredicate)
                .forEach(appInfo -> {
                    packageAssetService.getPackageAssets(appInfo.getPackageName());
                    appAssetUpdateEventManager.publishUpdate(
                            appInfo.getPackageName(),
                            ImmutableSet.of(AssetType.APP_NAME, AssetType.ICON));
                });
    }

}
