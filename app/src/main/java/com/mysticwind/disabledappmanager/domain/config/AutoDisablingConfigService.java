package com.mysticwind.disabledappmanager.domain.config;

public interface AutoDisablingConfigService {
    boolean isAutoDisablingOn();
    void setAutoDisablingState(boolean state);
    long getAutoDisablingTimeoutInSeconds();
    void setAutoDisablingTimeout(long timeoutInSeconds);
}
