package com.mysticwind.disabledappmanager.config;

import android.app.Service;
import android.content.pm.PackageManager;

import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;
import com.mysticwind.disabledappmanager.domain.state.DecisionObserver;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.TimerTriggeredDisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.timer.AndroidHandlerTimerManager;
import com.mysticwind.disabledappmanager.domain.timer.TimerManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppSwitchDetectionServiceModule {
    private final Service service;
    private final DecisionObserver decisionObserver;

    public AppSwitchDetectionServiceModule(Service service, DecisionObserver decisionObserver) {
        this.service = service;
        this.decisionObserver = decisionObserver;
    }

    @Provides @Singleton
    public PackageManager providePackageManager() {
        return this.service.getPackageManager();
    }

    @Provides @Singleton
    public DecisionObserver provideDecisionObserver() {
        return this.decisionObserver;
    }

    @Provides @Singleton
    public TimerManager provideTimerManager() {
        return new AndroidHandlerTimerManager();
    }

    @Provides @Singleton
    public DisabledPackageStateDecider provideDisabledPackageStateDecider(
            DecisionObserver decisionObserver, TimerManager timerManager) {
        return new TimerTriggeredDisabledPackageStateDecider(decisionObserver, timerManager);
    }

    @Provides @Singleton
    public AppStateProvider provideAppStateProvider(PackageManager packageManager) {
        return new PackageMangerAppStateProvider(packageManager);
    }

    @Provides @Singleton
    public PackageStateController providePackageStateController() {
        return new RootProcessPackageStateController();
    }
}
