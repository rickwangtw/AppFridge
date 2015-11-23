package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerDisabledPackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerEnabledPackageListProvider;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;

import org.androidannotations.annotations.EActivity;

@EActivity
public class PackageStatePerspective extends PerspectiveBase {

    private LayoutInflater layoutInflater;
    private PackageListProvider defaultPackageListProvider;
    private PackageListProvider packageListProvider;
    private AppSelectedListener appSelectedListener;
    private AppListAdapter appListAdapter;
    private int[] appStatusChangingButtonResourceIds = {
            R.id.toggle_app_state_button,
            R.id.disable_app_button,
            R.id.enable_app_button};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perspective_state_activity);

        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        defaultPackageListProvider = new PackageManagerAllPackageListProvider(getPackageManager());

        appSelectedListener = new AppSelectedListener(this, layoutInflater, packageStateController,
                appStateProvider, appGroupManager, manualStateUpdateEventManager);

        Button addToGroupButton = (Button) findViewById(R.id.add_to_group_button);
        addToGroupButton.setOnClickListener(appSelectedListener);

        Button toggleAppStatusButton = (Button) findViewById(R.id.toggle_app_state_button);
        toggleAppStatusButton.setOnClickListener(appSelectedListener);

        Button enableAppStatusButton = (Button) findViewById(R.id.enable_app_button);
        enableAppStatusButton.setOnClickListener(appSelectedListener);

        Button disableAppStatusButton = (Button) findViewById(R.id.disable_app_button);
        disableAppStatusButton.setOnClickListener(appSelectedListener);

        Spinner appStatusSpinner = (Spinner) findViewById(R.id.app_status_spinner);
        appStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int showingButtonResourceId = R.id.toggle_app_state_button;

                switch (position) {
                    case 1:
                        showingButtonResourceId = R.id.disable_app_button;
                        packageListProvider
                                = new PackageManagerEnabledPackageListProvider(getPackageManager());
                        break;
                    case 2:
                        showingButtonResourceId = R.id.enable_app_button;
                        packageListProvider
                                = new PackageManagerDisabledPackageListProvider(getPackageManager());
                        break;
                    default:
                        packageListProvider = defaultPackageListProvider;
                        break;
                }
                generateListView(showingButtonResourceId);
                if (searchBarDisplayed) {
                    appListAdapter.doSearch(searchEditText.getText().toString());
                } else {
                    appListAdapter.cancelSearch();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        packageListProvider = defaultPackageListProvider;
        generateListView(R.id.toggle_app_state_button);
    }

    private void generateListView(int showingButtonResourceId) {
        appListAdapter = new AppListAdapter(this,
                packageListProvider, appStateProvider, appIconProvider,
                appNameProvider, appLauncher, layoutInflater, appSelectedListener);

        appSelectedListener.deleteObservers();
        appSelectedListener.addObserver(appListAdapter);

        appAssetUpdateEventManager.registerListener(appListAdapter.getAppAssetUpdateListener());
        packageStateUpdateEventManager.registerListener(appListAdapter.getPackageStateUpdateListener());

        ListView appListView = (ListView)findViewById(R.id.appListView);
        appListView.setAdapter(appListAdapter);

        for (int buttonResourceId : appStatusChangingButtonResourceIds) {
            int visibility = View.GONE;
            if (buttonResourceId == showingButtonResourceId) {
                visibility = View.VISIBLE;
            }
            findViewById(buttonResourceId).setVisibility(visibility);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.perspective_state_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        switch (item.getItemId()) {
            case R.id.action_switch_perspective:
                startActivity(new Intent(this, AppGroupPerspective_.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void performSearch(String searchQuery) {
        appListAdapter.doSearch(searchQuery);
    }

    @Override
    protected void cancelSearch() {
        appListAdapter.cancelSearch();
    }
}
