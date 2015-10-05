package com.mysticwind.disabledappmanager.ui.activity.perspective;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.config.DaggerPerspectiveCommonComponent;
import com.mysticwind.disabledappmanager.config.PerspectiveCommonComponent;
import com.mysticwind.disabledappmanager.config.PerspectiveCommonModule;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.ui.activity.HelpActivity;
import com.mysticwind.disabledappmanager.ui.activity.settings.SettingsActivity_;

public class PerspectiveBase extends AppCompatActivity {

    private PerspectiveCommonComponent component;
    protected AppIconProvider appIconProvider;
    protected AppNameProvider appNameProvider;
    protected PackageStateController packageStateController;
    protected AppStateProvider appStateProvider;
    protected AppLauncher appLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        component = DaggerPerspectiveCommonComponent.builder()
                .perspectiveCommonModule(new PerspectiveCommonModule(this))
                .build();

        this.appIconProvider = component.appIconProvider();
        this.appNameProvider = component.appNameProvider();
        this.packageStateController = component.packageStateController();
        this.appStateProvider = component.appStateProvider();
        this.appLauncher = component.appLauncher();
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
