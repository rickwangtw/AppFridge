package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.content.Context;
import android.content.res.Configuration;
import android.preference.PreferenceActivity;

import com.mysticwind.disabledappmanager.R;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.PreferenceHeaders;

import lombok.extern.slf4j.Slf4j;


/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p/>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
@PreferenceHeaders(R.xml.pref_headers)
@EActivity
@Slf4j
public class SettingsActivity extends PreferenceActivity {

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return true;
    }
}
