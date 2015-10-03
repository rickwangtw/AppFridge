package com.mysticwind.disabledappmanager.config;

import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppSwitchDetectionServiceModule.class)
public interface AppSwitchDetectionServiceComponent {
    DisabledPackageStateDecider disabledPackageStateDecider();
    AppStateProvider appStateProvider();
    PackageStateController packageStateController();
}
