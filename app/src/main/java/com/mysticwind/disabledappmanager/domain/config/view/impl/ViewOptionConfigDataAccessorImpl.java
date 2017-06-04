package com.mysticwind.disabledappmanager.domain.config.view.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.config.view.ViewOptionConfigDataAccessor;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ViewOptionConfigDataAccessorImpl implements ViewOptionConfigDataAccessor {

    private static final String PACKAGE_NAME_ORDERING_METHOD = "PACKAGE_NAME";
    private static final String APPLICATION_LABEL_ORDERING_METHOD = "APPLICATION_LABEL";

    private static final BiMap<String, ApplicationOrderingMethod> ORDERING_METHOD_MAP = ImmutableBiMap.of(
            PACKAGE_NAME_ORDERING_METHOD, ApplicationOrderingMethod.PACKAGE_NAME,
            APPLICATION_LABEL_ORDERING_METHOD, ApplicationOrderingMethod.APPLICATION_LABEL
    );

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

    @Override
    public ApplicationOrderingMethod getOrderingMethod() {
        String orderingMethodString = viewOptionConfig.orderingMethod().get();
        return ORDERING_METHOD_MAP.getOrDefault(orderingMethodString, ApplicationOrderingMethod.PACKAGE_NAME);
    }

    @Override
    public void setOrderingMethod(ApplicationOrderingMethod applicationOrderingMethod) {
        String orderingMethodString = ORDERING_METHOD_MAP.inverse().get(applicationOrderingMethod);
        if (orderingMethodString == null) {
            throw new IllegalArgumentException("Unsupported ordering method: " + applicationOrderingMethod);
        }
        viewOptionConfig.edit()
                .orderingMethod()
                .put(orderingMethodString)
                .apply();
    }
}
