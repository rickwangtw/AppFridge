package com.mysticwind.disabledappmanager.domain.appgroup;

import lombok.Value;

@Value
public class AppGroupUpdate {
    private final String appGroupName;
    private final AppGroupOperation operation;
}
