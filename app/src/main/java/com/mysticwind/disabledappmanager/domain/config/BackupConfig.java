package com.mysticwind.disabledappmanager.domain.config;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

@SharedPref(SharedPref.Scope.UNIQUE)
public interface BackupConfig {
    String backupPath();
}
