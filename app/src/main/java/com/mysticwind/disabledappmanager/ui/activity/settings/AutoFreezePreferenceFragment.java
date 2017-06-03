package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.app.Dialog;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

import com.google.common.base.Splitter;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.service.AppSwitchDetectionService;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceScreen;

import lombok.extern.slf4j.Slf4j;

@PreferenceScreen(R.xml.pref_autofreeze)
@EFragment
@Slf4j
public class AutoFreezePreferenceFragment extends PreferenceFragment {

    private AutoDisablingConfigService autoDisablingConfigService;
    private Dialog goToAccessibilitySettingsDialog;

    @PreferenceByKey(R.string.pref_key_enable_app_launch_autodisable)
    CheckBoxPreference enableAppLaunchAutoDisablePreference;

    @PreferenceByKey(R.string.pref_key_auto_disable_timeout)
    ListPreference autoDisableTimeoutPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        autoDisablingConfigService = ApplicationHelper.from(this).autoDisablingConfigService();

        getPreferenceManager().setSharedPreferencesName(AutoDisablingConfig.class.getSimpleName());

        goToAccessibilitySettingsDialog = DialogHelper.newGoToAccessibilitySettings(getActivity());
    }

    @AfterPreferences
    void configurePreferences() {
        enableAppLaunchAutoDisablePreference.setChecked(
                autoDisablingConfigService.isAutoDisablingOn());
        autoDisableTimeoutPreference.setValue(
                String.valueOf(autoDisablingConfigService.getAutoDisablingTimeoutInSeconds()));

        if (autoDisablingConfigService.isAutoDisablingOn()
                && !isAppSwitchDetectionAccessibilitySettingsEnabled()) {
            goToAccessibilitySettingsDialog.show();
        }

        enableAppLaunchAutoDisablePreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean enable = (Boolean) newValue;
                if (enable && !isAppSwitchDetectionAccessibilitySettingsEnabled()) {
                    goToAccessibilitySettingsDialog.show();
                    return false;
                }
                autoDisablingConfigService.setAutoDisablingState(enable);
                return true;
            }
        });
    }

    private boolean isAppSwitchDetectionAccessibilitySettingsEnabled() {
        final String appSwitchDetectionServiceFullName =
                getActivity().getPackageName() + "/" + AppSwitchDetectionService.class.getCanonicalName();
        try {
            if (Settings.Secure.getInt(
                    getActivity().getContentResolver(),
                    Settings.Secure.ACCESSIBILITY_ENABLED) != 1) {
                return false;
            }
        } catch (Settings.SettingNotFoundException e) {
            log.warn("Error finding accessibility settings", e);
            return false;
        }
        String allEnabledServicesDelimitedWithColon = Settings.Secure.getString(
                getActivity().getContentResolver(),
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES);

        Iterable<String> enabledServiceNames = Splitter.on(':').split(allEnabledServicesDelimitedWithColon);

        for (String enabledServiceName : enabledServiceNames) {
            if (appSwitchDetectionServiceFullName.equals(enabledServiceName)) {
                return true;
            }
        }

        return false;
    }

    @PreferenceChange(R.string.pref_key_auto_disable_timeout)
    void onDisableTimeoutPreferenceChanged(Preference preference, long newTimeout) {
        autoDisablingConfigService.setAutoDisablingTimeout(newTimeout);
    }
}
