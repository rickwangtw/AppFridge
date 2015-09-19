package com.mysticwind.disabledappmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;

import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppIconProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppNameProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ListView appListView = (ListView)findViewById(R.id.appListView);

        List<ApplicationInfo> packages = getPackageManager()
                .getInstalledApplications(PackageManager.GET_META_DATA);

        List<String> packageName = new ArrayList<>(packages.size());
        for (ApplicationInfo appInfo : packages) {
            packageName.add(appInfo.packageName);
        }

        AppStateProvider appStateProvider = new PackageMangerAppStateProvider(getPackageManager());
        AppNameProvider appNameProvider = new PackageMangerAppNameProvider(getPackageManager());
        AppIconProvider appIconProvider = new PackageMangerAppIconProvider(getPackageManager());
        PackageStateController packageStateController = new RootProcessPackageStateController();

        AppSelectedListener appSelectedListener = new AppSelectedListener(
                packageStateController, appStateProvider);

        AppListAdapter appListAdapter = new AppListAdapter(appStateProvider, appIconProvider, appNameProvider,
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), packages,
                appSelectedListener);

        appSelectedListener.addObserver(appListAdapter);

        appListView.setAdapter(appListAdapter);

        Button enableAppButton = (Button) findViewById(R.id.toggle_app_state_button);
        enableAppButton.setOnClickListener(appSelectedListener);

        Button disableAppButton = (Button) findViewById(R.id.disable_app_button);
        disableAppButton.setOnClickListener(appSelectedListener);
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
