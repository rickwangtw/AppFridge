package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.config.application.AppStateConfigDataAccessor;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspectiveTutorial;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;

public class PerspectiveSelector extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppStateConfigDataAccessor appStateDataAccessor = ApplicationHelper.from(this).appStateConfigDataAccessor();
        AppGroupManager appGroupManager = ApplicationHelper.from(this).appGroupManager();

        if (appStateDataAccessor.shouldShowPackageStatePerspectiveTutorial()) {
            startActivity(new Intent(this, PackageStatePerspectiveTutorial.class));
        } else if (appGroupManager.getAllAppGroups().isEmpty()) {
            startActivity(new Intent(this, PackageStatePerspective_.class));
        } else {
            startActivity(new Intent(this, AppGroupPerspective_.class));
        }
        finish();
    }

}
