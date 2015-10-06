package com.mysticwind.disabledappmanager.config;

import android.content.Context;
import android.content.pm.PackageManager;

import com.gmr.acacia.Acacia;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.AutoDisablingAppLauncher;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;
import com.mysticwind.disabledappmanager.domain.config.AnnotationGeneratedConfigAutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessorImpl;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.TimerTriggeredDisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.timer.AndroidHandlerTimerManager;
import com.mysticwind.disabledappmanager.domain.timer.TimerManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ApplicationModule {
    private final Context context;
    private final AutoDisablingConfig_ autoDisablingConfig;

    public ApplicationModule(Context context, AutoDisablingConfig_ autoDisablingConfig) {
        this.context = context;
        this.autoDisablingConfig = autoDisablingConfig;
    }

    @Provides @Singleton
    public Context provideContext() {
        return this.context;
    }

    @Provides @Singleton
    public AutoDisablingConfig_ provideAutoDisablingConfig() {
        return this.autoDisablingConfig;
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
    public AutoDisablingConfigService provideAutoDisablingConfigService(
            AutoDisablingConfigDataAccessor autoDisablingConfigDataAccessor) {
        return new AnnotationGeneratedConfigAutoDisablingConfigService(autoDisablingConfigDataAccessor);
    }

    @Provides @Singleton
    public AppLauncher provideAppLauncher(
            AutoDisablingConfigService autoDisablingConfigService,
            PackageManager packageManager,
            AppStateProvider appStateProvider,
            PackageStateController packageStateController) {
        return new AutoDisablingAppLauncher(autoDisablingConfigService,
                packageManager, appStateProvider, packageStateController);
    }

    @Provides @Singleton
    public TimerManager provideTimerManager() {
        return new AndroidHandlerTimerManager();
    }

    @Provides @Singleton
    public DisabledPackageStateDecider provideDisabledPackageStateDecider(TimerManager timerManager) {
        return new TimerTriggeredDisabledPackageStateDecider(timerManager);
    }

    @Provides @Singleton
    public AutoDisablingConfigDataAccessor provideAutoDisablingConfigDataAccessor(
            AutoDisablingConfig_ autoDisablingConfig) {
        return new AutoDisablingConfigDataAccessorImpl(autoDisablingConfig);
    }
}
