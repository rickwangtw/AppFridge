package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mysticwind.disabledappmanager.BuildConfig;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.FirstLaunchOptimizedPackageStatePerspective_;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;

public class PerspectiveSelector extends AppCompatActivity {

    private static final String APPLICATION_STATE_PREFERENCE_NAME = "ApplicationState";
    private static final String VERSION_CODE_PREFERENCE_KEY = "versionCode";
    private static final int NO_VERSION_CODE = -1;
    private static final int CURRENT_VERSION_CODE = BuildConfig.VERSION_CODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppGroupManager appGroupManager = ApplicationHelper.from(this).appGroupManager();
        if (isFirstRun()) {
            startActivity(new Intent(this, FirstLaunchOptimizedPackageStatePerspective_.class));
        } else if (appGroupManager.getAllAppGroups().isEmpty()) {
            startActivity(new Intent(this, PackageStatePerspective_.class));
        } else {
            startActivity(new Intent(this, AppGroupPerspective_.class));
        }
        finish();
    }

    protected boolean isFirstRun() {
        final SharedPreferences preferences = getSharedPreferences(APPLICATION_STATE_PREFERENCE_NAME, MODE_PRIVATE);
        final int persistedVersionCode = preferences.getInt(VERSION_CODE_PREFERENCE_KEY, NO_VERSION_CODE);

        final boolean isFirstRun;
        if (CURRENT_VERSION_CODE == persistedVersionCode) {
            // normal run
            isFirstRun = false;
        } else if (NO_VERSION_CODE == persistedVersionCode) {
            // new install or cleared data
            isFirstRun = true;
        } else if (CURRENT_VERSION_CODE > persistedVersionCode) {
            // upgrade
            isFirstRun = false;
        } else {
            isFirstRun = false;
        }

        preferences.edit()
                .putInt(VERSION_CODE_PREFERENCE_KEY, CURRENT_VERSION_CODE)
                .apply();
        return isFirstRun;
    }
}
