package com.mysticwind.disabledappmanager;

import android.app.Application;
import android.content.Context;

import com.mysticwind.disabledappmanager.config.ApplicationComponent;
import com.mysticwind.disabledappmanager.config.ApplicationModule;
import com.mysticwind.disabledappmanager.config.DaggerApplicationComponent;
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

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EApplication
public class AppFridgeApplication extends Application implements ApplicationComponent {
    private ApplicationComponent component;

    @Pref
    AutoDisablingConfig_ autoDisablingConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this, autoDisablingConfig))
                .build();
    }

    @Override
    public void inject(Context context) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public void inject(AutoDisablingConfig_ autoDisablingConfig) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public PackageAssetService packageAssetService() {
        return component.packageAssetService();
    }

    @Override
    public AppIconProvider appIconProvider() {
        return component.appIconProvider();
    }

    @Override
    public AppNameProvider appNameProvider() {
        return component.appNameProvider();
    }

    @Override
    public AppStateProvider appStateProvider() {
        return component.appStateProvider();
    }

    @Override
    public PackageStateController packageStateController() {
        return component.packageStateController();
    }

    @Override
    public AppLauncher appLauncher() {
        return component.appLauncher();
    }

    @Override
    public DisabledPackageStateDecider disabledPackageStateDecider() {
        return component.disabledPackageStateDecider();
    }

    @Override
    public AutoDisablingConfigService autoDisablingConfigService() {
        return component.autoDisablingConfigService();
    }

    @Override
    public ManualStateUpdateEventManager manualStateUpdateEventManager() {
        return component.manualStateUpdateEventManager();
    }
}
