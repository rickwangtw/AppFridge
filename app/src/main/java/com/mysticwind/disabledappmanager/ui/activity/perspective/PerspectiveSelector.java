package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;

public class PerspectiveSelector extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppGroupManager appGroupManager = ApplicationHelper.from(this).appGroupManager();
        if (appGroupManager.getAllAppGroups().isEmpty()) {
            startActivity(new Intent(this, PackageStatePerspective_.class));
        } else {
            startActivity(new Intent(this, AppGroupPerspective_.class));
        }
        finish();
    }
}
