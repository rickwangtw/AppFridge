package com.mysticwind.disabledappmanager.config;

import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = GeneralPreferenceFragmentModule.class)
public interface GeneralPreferenceFragmentComponent {
    void inject(AutoDisablingConfig_ autoDisablingConfig);
    AutoDisablingConfigService autoDisablingConfigService();
}
