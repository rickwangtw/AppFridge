package com.mysticwind.disabledappmanager.domain.config.view;

import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;

public interface ViewOptionConfigDataAccessor {

    boolean showSystemApps();
    void setShowSystemApps(boolean showSystemApps);

    ApplicationOrderingMethod getOrderingMethod();
    void setOrderingMethod(ApplicationOrderingMethod applicationOrderingMethod);

}
