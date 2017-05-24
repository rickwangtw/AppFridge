package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateActivityBinding;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModelList;

import org.androidannotations.annotations.EActivity;

@EActivity
public class PackageStatePerspective extends PerspectiveBase {

    private PackageListProvider packageListProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perspective_state_activity);


        packageListProvider = new PackageManagerAllPackageListProvider(getPackageManager());

        PerspectiveStateActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_state_activity);
        ApplicationModelList list = new ApplicationModelList(packageListProvider,
                packageAssetService, appAssetUpdateEventManager, packageStateController,
                appStateProvider, packageStateUpdateEventManager, appGroupManager,
                DialogHelper.newProgressDialog(PackageStatePerspective.this));
        binding.setApplications(list);
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
        // TODO
    }

    @Override
    protected void cancelSearch() {
        // TODO
    }
}
