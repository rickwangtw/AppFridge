package com.mysticwind.disabledappmanager.domain.model;

import lombok.Value;

@Value
public class AppInfo {
    private final String packageName;
    private final boolean enabled;
    private final boolean isSystemApp;
}
