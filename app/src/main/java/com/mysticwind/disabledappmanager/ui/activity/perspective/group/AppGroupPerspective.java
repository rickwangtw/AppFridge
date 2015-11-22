package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.common.SwipeDetector;

import org.androidannotations.annotations.EActivity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EActivity
public class AppGroupPerspective extends PerspectiveBase {

    private PackageListProvider packageListProvider;
    private AppGroupListAdapter appGroupListAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perspective_appgroup_activity);

        this.packageListProvider = new PackageManagerAllPackageListProvider(getPackageManager());

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.appGroupListView);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        SwipeDetector swipeDetector = new SwipeDetector();
        appGroupListAdapter = new AppGroupListAdapter(this, appGroupManager,
                appIconProvider, appNameProvider, appStateProvider, packageListProvider,
                packageStateController, appLauncher, manualStateUpdateEventManager, layoutInflater, swipeDetector);

        appGroupUpdateEventManager.registerListener(appGroupListAdapter.getAppGroupUpdateListener());
        appAssetUpdateEventManager.registerListener(appGroupListAdapter.getAppAssetUpdateListener());

        listView.setAdapter(appGroupListAdapter);
        listView.setOnItemLongClickListener(appGroupListAdapter);
        listView.setOnGroupClickListener(appGroupListAdapter);
        listView.setOnChildClickListener(appGroupListAdapter);
        listView.setOnTouchListener(swipeDetector);
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
                        appNameProvider, appGroupManager, appGroupListAdapter).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void performSearch(String searchQuery) {
        appGroupListAdapter.doSearch(searchQuery);
    }

    @Override
    protected void cancelSearch() {
        appGroupListAdapter.cancelSearch();
    }
}
