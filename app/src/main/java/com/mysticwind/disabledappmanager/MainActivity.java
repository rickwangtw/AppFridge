package com.mysticwind.disabledappmanager;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

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

        appListView.setAdapter(new AppListAdapter(getPackageManager(),
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE), packages));
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
