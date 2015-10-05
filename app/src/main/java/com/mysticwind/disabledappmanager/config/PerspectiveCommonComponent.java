package com.mysticwind.disabledappmanager.config;

import android.content.Context;

import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.PackageStateController;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = PerspectiveCommonModule.class)
public interface PerspectiveCommonComponent {
    void inject(Context context);
    PackageAssetService packageAssetService();
    AppIconProvider appIconProvider();
    AppNameProvider appNameProvider();
    AppStateProvider appStateProvider();
    PackageStateController packageStateController();
    AppLauncher appLauncher();
}
