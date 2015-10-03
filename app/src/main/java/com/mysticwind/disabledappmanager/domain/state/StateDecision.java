package com.mysticwind.disabledappmanager.domain.state;

import lombok.NonNull;
import lombok.Value;

@Value
public class StateDecision {
    @NonNull private String packageName;
    @NonNull private PackageState decidedState;
}
