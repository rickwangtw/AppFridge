package com.mysticwind.disabledappmanager.domain.config;

public class AnnotationGeneratedConfigAutoDisablingConfigService implements AutoDisablingConfigService {
    private static final boolean DEFAULT_AUTO_DISABLING_STATE = false;
    private static final int DEFAULT_DISABLE_TIMEOUT_IN_SECONDS = 60;

    private final AutoDisablingConfigDataAccessor configDataAccessor;

    public AnnotationGeneratedConfigAutoDisablingConfigService(
            AutoDisablingConfigDataAccessor configDataAccessor) {
        this.configDataAccessor = configDataAccessor;
    }

    @Override
    public boolean isAutoDisablingOn() {
        return configDataAccessor.isAutoDisablingState(DEFAULT_AUTO_DISABLING_STATE);
    }

    @Override
    public void setAutoDisablingState(boolean state) {
        configDataAccessor.setAutoDisablingState(state);
    }

    @Override
    public long getAutoDisablingTimeoutInSeconds() {
        return configDataAccessor.getAutoDisablingTimeout(DEFAULT_DISABLE_TIMEOUT_IN_SECONDS);
    }

    @Override
    public void setAutoDisablingTimeout(long timeoutInSeconds) {
        configDataAccessor.setAutoDisablingTimeout(timeoutInSeconds);
    }
}
