package com.mysticwind.disabledappmanager.domain.state;

import lombok.Value;

@Value
public class ManualStateUpdate {
    private final String packageName;
    private final PackageState packageState;
}
