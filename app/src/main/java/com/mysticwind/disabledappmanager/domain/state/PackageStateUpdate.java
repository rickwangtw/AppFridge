package com.mysticwind.disabledappmanager.domain.state;

import lombok.Value;

@Value
public class PackageStateUpdate {
    private final String appGroupName;
    private final PackageState packageState;
}
