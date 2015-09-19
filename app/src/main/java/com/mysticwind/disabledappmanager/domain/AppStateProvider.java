package com.mysticwind.disabledappmanager.domain;

public interface AppStateProvider {
    boolean isPackageEnabled(String packageName);
}
