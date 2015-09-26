package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppGroupManagerImpl;
import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective;

public class PerspectiveSelector extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppGroupManager appGroupManager = new AppGroupManagerImpl(new AppGroupDAO(this));
        if (appGroupManager.getAllAppGroups().isEmpty()) {
            startActivity(new Intent(this, PackageStatePerspective.class));
        } else {
            startActivity(new Intent(this, AppGroupPerspective.class));
        }
        finish();
    }
}
