package com.mysticwind.disabledappmanager.domain.state;

public class DisabledStateDetectionRequest {
    private final String packageName;
    private final long noActivityTimeoutInSeconds;

    public DisabledStateDetectionRequest(String packageName, long noActivityTimeoutInSeconds) {
        this.packageName = packageName;
        this.noActivityTimeoutInSeconds = noActivityTimeoutInSeconds;
    }

    public String getPackageName() {
        return packageName;
    }

    public long getNoActivityTimeoutInSeconds() {
        return noActivityTimeoutInSeconds;
    }
}
