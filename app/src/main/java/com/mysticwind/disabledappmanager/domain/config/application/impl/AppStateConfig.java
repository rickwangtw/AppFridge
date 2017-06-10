package com.mysticwind.disabledappmanager.domain.config.application.impl;

import org.androidannotations.annotations.sharedpreferences.DefaultInt;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface AppStateConfig {

    @DefaultInt(-1)
    int lastShownPackageStatePerspectiveTutorialVersion();

    @DefaultInt(-1)
    int lastShownAppGroupPerspectiveTutorialVersion();

}
