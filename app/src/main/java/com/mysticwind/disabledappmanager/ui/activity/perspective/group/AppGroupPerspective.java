package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.gmr.acacia.Acacia;
import com.mysticwind.disabledappmanager.domain.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.ui.activity.HelpActivity;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppGroupManagerImpl;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAppLauncher;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;
import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.common.SwipeDetector;

import de.greenrobot.event.EventBus;

public class AppGroupPerspective extends AppCompatActivity {
    private static final String TAG = "AppGroupPerspective";

    private PackageAssetService packageAssetService;
    private AppGroupManager appGroupManager;
    private AppIconProvider appIconProvider;
    private AppNameProvider appNameProvider;
    private AppStateProvider appStateProvider;
    private PackageStateController packageStateController;
    private PackageListProvider packageListProvider;
    private AppGroupListAdapter appGroupListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perspective_appgroup_activity);

        packageAssetService = Acacia.createService(this, PackageAssetService.class);

        this.appGroupManager = new AppGroupManagerImpl(new AppGroupDAO(this));

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.appGroupListView);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.appIconProvider = packageAssetService;
        this.appNameProvider = packageAssetService;
        this.appStateProvider = new PackageMangerAppStateProvider(getPackageManager());
        this.packageStateController = new RootProcessPackageStateController();
        this.packageListProvider = new PackageManagerAllPackageListProvider(getPackageManager());
        AppLauncher appLauncher = new PackageManagerAppLauncher(
                getPackageManager(), appStateProvider, packageStateController);

        SwipeDetector swipeDetector = new SwipeDetector();
        appGroupListAdapter = new AppGroupListAdapter(this, appGroupManager,
                appIconProvider, appNameProvider, appStateProvider, packageListProvider,
                packageStateController, appLauncher, layoutInflater, swipeDetector);
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
                startActivity(new Intent(this, PackageStatePerspective.class));
                return true;
            case R.id.action_new_app_group:
                DialogHelper.newNewAppGroupDialog(this, packageListProvider, appIconProvider,
                        appNameProvider, appGroupManager, appGroupListAdapter).show();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(this, HelpActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
