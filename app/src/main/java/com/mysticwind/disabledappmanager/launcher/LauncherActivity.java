package com.mysticwind.disabledappmanager.launcher;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppGroupManagerImpl;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.CachingAppInfoProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppIconProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppNameProvider;
import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;

public class LauncherActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher);

        AppGroupManager appGroupManager = new AppGroupManagerImpl(new AppGroupDAO(this));

        ExpandableListView listView = (ExpandableListView) findViewById(R.id.appGroupListView);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        CachingAppInfoProvider appInfoProvider = CachingAppInfoProvider.INSTANCE.init(
                new PackageMangerAppIconProvider(getPackageManager()),
                new PackageMangerAppNameProvider(getPackageManager()),
                getPackageManager());
        AppIconProvider appIconProvider = appInfoProvider;
        AppNameProvider appNameProvider = appInfoProvider;
        listView.setAdapter(new AppGroupListAdapter(appGroupManager, appIconProvider,
                appNameProvider, layoutInflater));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_launcher, menu);
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
