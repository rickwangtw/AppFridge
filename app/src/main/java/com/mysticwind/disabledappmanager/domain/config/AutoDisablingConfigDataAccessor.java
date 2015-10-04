package com.mysticwind.disabledappmanager.domain.config;

public interface AutoDisablingConfigDataAccessor {
    boolean isAutoDisablingState(boolean defaultValue);
    void setAutoDisablingState(boolean enable);
    long getAutoDisablingTimeout(long defaultTimeoutInSeconds);
    void setAutoDisablingTimeout(long timeoutInSeconds);
}
