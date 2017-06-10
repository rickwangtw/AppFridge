package com.mysticwind.disabledappmanager.domain.config.application.impl;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.BuildConfig;
import com.mysticwind.disabledappmanager.domain.config.application.AppStateConfigDataAccessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppStateConfigDataAccessorImpl implements AppStateConfigDataAccessor {

    private static final int CURRENT_VERSION_CODE = BuildConfig.VERSION_CODE;

    private final AppStateConfig_ appStateConfig;

    public AppStateConfigDataAccessorImpl(final AppStateConfig_ appStateConfig) {
        this.appStateConfig = Preconditions.checkNotNull(appStateConfig);
    }

    @Override
    public boolean shouldShowPackageStatePerspectiveTutorial() {
        int lastShownVersion = appStateConfig.lastShownPackageStatePerspectiveTutorialVersion().get();

        if (lastShownVersion < CURRENT_VERSION_CODE) {
            return true;
        }
        return false;
    }

    @Override
    public void updatePackageStatePerspectiveTutorialShown() {
        appStateConfig.edit()
                .lastShownPackageStatePerspectiveTutorialVersion()
                .put(CURRENT_VERSION_CODE)
                .apply();
    }

    @Override
    public boolean shouldShowAppGroupPerspectiveTutorial() {
        int lastShownVersion = appStateConfig.lastShownAppGroupPerspectiveTutorialVersion().get();

        if (lastShownVersion < CURRENT_VERSION_CODE) {
            return true;
        }
        return false;
    }

    @Override
    public void updateAppGroupPerspectiveTutorialShown() {
        appStateConfig.edit()
                .lastShownAppGroupPerspectiveTutorialVersion()
                .put(CURRENT_VERSION_CODE)
                .apply();
    }

}
