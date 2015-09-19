package com.mysticwind.disabledappmanager;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;

import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.CachingAppInfoProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppIconProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppNameProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerPackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private PackageListProvider packageListProvider;
    private AppStateProvider appStateProvider;
    private AppNameProvider appNameProvider;
    private AppIconProvider appIconProvider;
    private PackageStateController packageStateController;
    private ProgressDialog progressDialog;
    private AppSelectedListener appSelectedListener;
    private int[] appStatusChangingButtonResourceIds = {
            R.id.toggle_app_state_button,
            R.id.disable_app_button,
            R.id.enable_app_button};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packageListProvider = new PackageMangerPackageListProvider(getPackageManager());
        appStateProvider = new PackageMangerAppStateProvider(getPackageManager());

        CachingAppInfoProvider appInfoProvider = CachingAppInfoProvider.INSTANCE.init(
                new PackageMangerAppIconProvider(getPackageManager()),
                new PackageMangerAppNameProvider(getPackageManager()),
                getPackageManager());
        appIconProvider = appInfoProvider;
        appNameProvider = appInfoProvider;

        packageStateController = new RootProcessPackageStateController();

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Updating application status");
        progressDialog.setIndeterminate(true);

        appSelectedListener = new AppSelectedListener(progressDialog,
                packageStateController, appStateProvider);

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
                List<AppInfo>  selectedPackages;
                int showingButtonResourceId = R.id.toggle_app_state_button;

                switch (position) {
                    case 1:
                        showingButtonResourceId = R.id.disable_app_button;
                        selectedPackages = packageListProvider.getOrderedEnabledPackages();
                        break;
                    case 2:
                        showingButtonResourceId = R.id.enable_app_button;
                        selectedPackages = packageListProvider.getOrderedDisabledPackages();
                        break;
                    default:
                        selectedPackages = packageListProvider.getOrderedAllPackages();
                        break;
                }
                generateListView(selectedPackages, showingButtonResourceId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        generateListView(packageListProvider.getOrderedAllPackages(), R.id.toggle_app_state_button);
    }

    private void generateListView(List<AppInfo> selectedPackages, int showingButtonResourceId) {
        AppListAdapter appListAdapter = new AppListAdapter(appStateProvider, appIconProvider,
                appNameProvider, (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE),
                selectedPackages, appSelectedListener);

        appSelectedListener.deleteObservers();
        appSelectedListener.addObserver(appListAdapter);

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
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
