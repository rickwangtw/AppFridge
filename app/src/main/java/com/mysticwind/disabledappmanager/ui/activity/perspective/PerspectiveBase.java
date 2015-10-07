package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.ui.activity.HelpActivity;
import com.mysticwind.disabledappmanager.ui.activity.settings.SettingsActivity_;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;

public abstract class PerspectiveBase extends AppCompatActivity {

    protected AppIconProvider appIconProvider;
    protected AppNameProvider appNameProvider;
    protected PackageStateController packageStateController;
    protected AppStateProvider appStateProvider;
    protected AppLauncher appLauncher;
    protected ManualStateUpdateEventManager manualStateUpdateEventManager;
    protected AppGroupManager appGroupManager;
    protected AppGroupUpdateEventManager appGroupUpdateEventManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.appIconProvider = ApplicationHelper.from(this).appIconProvider();
        this.appNameProvider = ApplicationHelper.from(this).appNameProvider();
        this.packageStateController = ApplicationHelper.from(this).packageStateController();
        this.appStateProvider = ApplicationHelper.from(this).appStateProvider();
        this.appLauncher = ApplicationHelper.from(this).appLauncher();
        this.manualStateUpdateEventManager = ApplicationHelper.from(this).manualStateUpdateEventManager();
        this.appGroupManager = ApplicationHelper.from(this).appGroupManager();
        this.appGroupUpdateEventManager = ApplicationHelper.from(this).appGroupUpdateEventManager();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingsActivity_.class));
                return true;
            case R.id.action_help:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
