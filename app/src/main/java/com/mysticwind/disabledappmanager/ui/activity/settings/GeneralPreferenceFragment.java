package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.provider.Settings;

import com.google.common.base.Splitter;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.config.DaggerGeneralPreferenceFragmentComponent;
import com.mysticwind.disabledappmanager.config.GeneralPreferenceFragmentComponent;
import com.mysticwind.disabledappmanager.config.GeneralPreferenceFragmentModule;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.service.AppSwitchDetectionService;

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
    private Dialog goToAccessibilitySettingsDialog;

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

        goToAccessibilitySettingsDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Please enable the service in accessibility settings")
                .setMessage("The service must be enabled to detect the switching of apps!")
                .setPositiveButton("Go to Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAccessibilitySettings();
                    }
                })
                .setNegativeButton("Cancel", null).create();
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

    private void startAccessibilitySettings() {
        startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
    }

    @PreferenceChange(R.string.pref_key_auto_disable_timeout)
    void onDisableTimeoutPreferenceChanged(Preference preference, long newTimeout) {
        autoDisablingConfigService.setAutoDisablingTimeout(newTimeout);
    }
}
