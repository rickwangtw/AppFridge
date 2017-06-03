package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.mysticwind.disabledappmanager.R;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceChange;
import org.androidannotations.annotations.PreferenceScreen;

import lombok.extern.slf4j.Slf4j;

@PreferenceScreen(R.xml.pref_general)
@EFragment
@Slf4j
public class GeneralPreferenceFragment extends PreferenceFragment {

    @PreferenceByKey(R.string.pref_key_viewoptions_show_system)
    CheckBoxPreference showSystemAppPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPreferenceManager().setSharedPreferencesName("ViewOptionsConfig");
    }

    @AfterPreferences
    void configurePreferences() {
        showSystemAppPreference.setChecked(false);
    }

    @PreferenceChange(R.string.pref_key_viewoptions_show_system)
    void onShowSystemAppPreferenceChanged(Preference preference, boolean enable) {
        log.info("Show System preference changed: " + enable);
    }
}
