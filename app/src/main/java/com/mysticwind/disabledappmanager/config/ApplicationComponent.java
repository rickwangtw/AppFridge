package com.mysticwind.disabledappmanager.config;

import android.content.Context;

import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class })
public interface ApplicationComponent {
    void inject(Context context);
    void inject(AutoDisablingConfig_ autoDisablingConfig);
    PackageAssetService packageAssetService();
    AppIconProvider appIconProvider();
    AppNameProvider appNameProvider();
    AppStateProvider appStateProvider();
    PackageStateController packageStateController();
    AppLauncher appLauncher();
    DisabledPackageStateDecider disabledPackageStateDecider();
    AutoDisablingConfigService autoDisablingConfigService();
    ManualStateUpdateEventManager manualStateUpdateEventManager();
}
