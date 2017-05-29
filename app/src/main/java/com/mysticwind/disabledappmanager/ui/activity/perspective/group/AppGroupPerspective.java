package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupActivityBinding;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;

import org.androidannotations.annotations.EActivity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EActivity
public class AppGroupPerspective extends PerspectiveBase {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PerspectiveAppgroupActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_appgroup_activity);
        binding.appGroupListView.setAdapter(
                new AppGroupListAdapter(this, appGroupManager, packageAssetService,
                        appStateProvider, packageListProvider, appLauncher,
                        (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
        );
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.perspective_appgroup_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch(item.getItemId()) {
            case R.id.action_switch_perspective:
                startActivity(new Intent(this, PackageStatePerspective_.class));
                return true;
            case R.id.action_new_app_group:
                DialogHelper.newNewAppGroupDialog(this, packageListProvider, appIconProvider,
                        appNameProvider, appGroupManager).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void performSearch(String searchQuery) {
        // TODO
    }

    @Override
    protected void cancelSearch() {
        // TODO
    }
}
