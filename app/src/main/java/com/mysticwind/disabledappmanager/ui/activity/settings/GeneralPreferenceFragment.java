package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.config.DaggerGeneralPreferenceFragmentComponent;
import com.mysticwind.disabledappmanager.config.GeneralPreferenceFragmentComponent;
import com.mysticwind.disabledappmanager.config.GeneralPreferenceFragmentModule;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceScreen;
import org.androidannotations.annotations.sharedpreferences.Pref;

import lombok.extern.slf4j.Slf4j;

@PreferenceScreen(R.xml.pref_general)
@EFragment
@Slf4j
public class GeneralPreferenceFragment extends PreferenceFragment {

    private GeneralPreferenceFragmentComponent component;
    private AutoDisablingConfigService autoDisablingConfigService;

    @Pref
    AutoDisablingConfig_ autoDisablingConfig;

    @PreferenceByKey(R.string.pref_key_enable_app_launch_autodisable)
    CheckBoxPreference enableAppLaunchAutoDisablePreference;

    @PreferenceByKey(R.string.pref_key_auto_disable_timeout)
    ListPreference autoDisableTimeoutPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName(AutoDisablingConfig.class.getSimpleName());
    }

    @AfterPreferences
    void configurePreferences() {
        component = DaggerGeneralPreferenceFragmentComponent.builder()
                .generalPreferenceFragmentModule(
                        new GeneralPreferenceFragmentModule(autoDisablingConfig))
                .build();

        autoDisablingConfigService = component.autoDisablingConfigService();

        enableAppLaunchAutoDisablePreference.setChecked(
                autoDisablingConfigService.isAutoDisablingOn());
        autoDisableTimeoutPreference.setValue(
                String.valueOf(autoDisablingConfigService.getAutoDisablingTimeoutInSeconds()));

        enableAppLaunchAutoDisablePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                autoDisablingConfigService.setAutoDisablingState((Boolean)newValue);
                return true;
            }
        });
    }

    @PreferenceChange(R.string.pref_key_auto_disable_timeout)
    void onDisableTimeoutPreferenceChanged(Preference preference, long newTimeout) {
        autoDisablingConfigService.setAutoDisablingTimeout(newTimeout);
    }
}
