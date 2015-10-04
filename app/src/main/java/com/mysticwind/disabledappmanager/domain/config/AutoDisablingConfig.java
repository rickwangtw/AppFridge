package com.mysticwind.disabledappmanager.domain.config;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultLong;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface AutoDisablingConfig {
    @DefaultBoolean(false)
    boolean isAutoDisablingOn();

    @DefaultLong(60L)
    long autoDisablingTimeoutInSeconds();
}
