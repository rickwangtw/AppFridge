package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.config.view.impl.ViewOptionConfig;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
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

        getPreferenceManager().setSharedPreferencesName(ViewOptionConfig.class.getSimpleName());
    }

}
