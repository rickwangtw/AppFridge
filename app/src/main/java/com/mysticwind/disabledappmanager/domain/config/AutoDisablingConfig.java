package com.mysticwind.disabledappmanager.domain.config;

import org.androidannotations.annotations.sharedpreferences.DefaultBoolean;
import org.androidannotations.annotations.sharedpreferences.DefaultString;
import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface AutoDisablingConfig {
    @DefaultBoolean(false)
    boolean isAutoDisablingOn();

    @DefaultString("60")
    String autoDisablingTimeoutInSeconds();
}
