package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;

import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.mysticwind.disabledappmanager.BR;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateActivityBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateAppItemBinding;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationStateViewModel;

import org.androidannotations.annotations.EActivity;

import java.util.Collections;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@EActivity
public class PackageStatePerspective extends PerspectiveBase {

    private ApplicationStateViewModel applicationStateViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.perspective_state_activity);

        PackageListProvider packageListProvider = new PackageManagerAllPackageListProvider(getPackageManager());

        PerspectiveStateActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_state_activity);
        binding.appListView.setLayoutManager(new LinearLayoutManager(this));

        RxDataSource<ApplicationModel> dataSource = new RxDataSource<>(Collections.emptyList());
        dataSource
                .<PerspectiveStateAppItemBinding>bindRecyclerView(binding.appListView, R.layout.perspective_state_app_item)
                .subscribe(viewHolder -> {
                    PerspectiveStateAppItemBinding itemBinding = viewHolder.getViewDataBinding();
                    ApplicationModel applicationModel = viewHolder.getItem();
                    itemBinding.setVariable(BR.application, applicationModel);
                    itemBinding.executePendingBindings();
                });
        applicationStateViewModel = new ApplicationStateViewModel(
                PackageStatePerspective.this, dataSource, packageListProvider,
                packageAssetService, appAssetUpdateEventManager, packageStateController,
                appStateProvider, packageStateUpdateEventManager, appGroupManager,
                DialogHelper.newProgressDialog(PackageStatePerspective.this), appLauncher);
        binding.setViewModel(applicationStateViewModel);
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
        if (applicationStateViewModel == null) {
            return;
        }
        applicationStateViewModel.performSearch(searchQuery);
    }

    @Override
    protected void cancelSearch() {
        if (applicationStateViewModel == null) {
            return;
        }
        applicationStateViewModel.cancelSearch();
    }
}
