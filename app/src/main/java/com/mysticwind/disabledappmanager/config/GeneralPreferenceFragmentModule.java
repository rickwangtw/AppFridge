package com.mysticwind.disabledappmanager.config;

import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessorImpl;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.config.AnnotationGeneratedConfigAutoDisablingConfigService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class GeneralPreferenceFragmentModule {
    private AutoDisablingConfig_ autoDisablingConfig;

    public GeneralPreferenceFragmentModule(AutoDisablingConfig_ autoDisablingConfig) {
        this.autoDisablingConfig = autoDisablingConfig;
    }

    @Provides @Singleton
    public AutoDisablingConfig_ provideAutoDisablingConfig() {
        return this.autoDisablingConfig;
    }

    @Provides @Singleton
    public AutoDisablingConfigDataAccessor provideAutoDisablingConfigDataAccessor(
            AutoDisablingConfig_ autoDisablingConfig) {
        return new AutoDisablingConfigDataAccessorImpl(autoDisablingConfig);
    }

    @Provides @Singleton
    public AutoDisablingConfigService provideAutoDisablingConfigService(
            AutoDisablingConfigDataAccessor autoDisablingConfigDataAccessor) {
        return new AnnotationGeneratedConfigAutoDisablingConfigService(autoDisablingConfigDataAccessor);
    }
}
