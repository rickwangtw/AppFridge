package com.mysticwind.disabledappmanager.domain.state;

import lombok.NonNull;
import lombok.Value;

@Value
public class DisabledStateDetectionRequest {
    @NonNull private final String packageName;
    private final long noActivityTimeoutInSeconds;
}
