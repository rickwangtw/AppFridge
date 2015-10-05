package com.mysticwind.disabledappmanager.config;

import android.content.Context;
import android.content.pm.PackageManager;

import com.gmr.acacia.Acacia;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.PackageManagerAppLauncher;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PerspectiveCommonModule {
    private final Context context;

    public PerspectiveCommonModule(Context context) {
        this.context = context;
    }

    @Provides @Singleton
    public Context provideContext() {
        return this.context;
    }

    @Provides @Singleton
    public PackageManager providePackageManager() {
        return this.context.getPackageManager();
    }

    @Provides @Singleton
    public PackageAssetService providePackageAssetService(Context context) {
        return Acacia.createService(context, PackageAssetService.class);
    }

    @Provides @Singleton
    public AppIconProvider provideAppIconProvider(PackageAssetService packageAssetService) {
        return packageAssetService;
    }

    @Provides @Singleton
    public AppNameProvider provideAppNameProvider(PackageAssetService packageAssetService) {
        return packageAssetService;
    }

    @Provides @Singleton
    public AppStateProvider provideAppStateProvider(PackageManager packageManager) {
        return new PackageMangerAppStateProvider(packageManager);
    }

    @Provides @Singleton
    public PackageStateController providePackageStateController() {
        return new RootProcessPackageStateController();
    }

    @Provides @Singleton
    public AppLauncher provideAppLauncher(PackageManager packageManager,
                                          AppStateProvider appStateProvider,
                                          PackageStateController packageStateController) {
        return new PackageManagerAppLauncher(
                packageManager, appStateProvider, packageStateController);
    }
}
