package com.mysticwind.disabledappmanager.domain.config.view.impl;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.domain.config.view.ViewOptionConfigDataAccessor;

public class ViewOptionConfigDataAccessorImpl implements ViewOptionConfigDataAccessor {

    private final ViewOptionConfig_ viewOptionConfig;

    public ViewOptionConfigDataAccessorImpl(final ViewOptionConfig_ viewOptionConfig) {
        this.viewOptionConfig = Preconditions.checkNotNull(viewOptionConfig);
    }

    @Override
    public boolean showSystemApps() {
        return viewOptionConfig
                .showSystemApps()
                .get();
    }

    @Override
    public void setShowSystemApps(boolean showSystemApps) {
        viewOptionConfig.edit()
                .showSystemApps()
                .put(showSystemApps)
                .apply();
    }
}
