package com.mysticwind.disabledappmanager.domain.config;

public class AutoDisablingConfigDataAccessorImpl implements AutoDisablingConfigDataAccessor {
    private final AutoDisablingConfig_ config;

    public AutoDisablingConfigDataAccessorImpl(AutoDisablingConfig_ config) {
        this.config = config;
    }

    @Override
    public boolean isAutoDisablingState(boolean defaultValue) {
        return config.isAutoDisablingOn().getOr(defaultValue);
    }

    @Override
    public void setAutoDisablingState(boolean enable) {
        config.edit()
                .isAutoDisablingOn()
                .put(enable)
                .apply();
    }

    @Override
    public long getAutoDisablingTimeout(long defaultTimeoutInSeconds) {
        String timeoutString = config.autoDisablingTimeoutInSeconds().get();
        if (timeoutString == null) {
            return defaultTimeoutInSeconds;
        } else {
            return Long.parseLong(timeoutString);
        }
    }

    @Override
    public void setAutoDisablingTimeout(long timeoutInSeconds) {
        config.edit()
                .autoDisablingTimeoutInSeconds()
                .put(String.valueOf(timeoutInSeconds))
                .apply();
    }
}
