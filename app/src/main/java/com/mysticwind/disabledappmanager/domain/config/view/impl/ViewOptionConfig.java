package com.mysticwind.disabledappmanager.domain.config.view.impl;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface ViewOptionConfig {

    @DefaultBoolean(false)
    boolean showSystemApps();

}
