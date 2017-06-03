package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;

import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.mysticwind.disabledappmanager.BR;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateActivityBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateAppGroupDialogBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveStateAppItemBinding;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.group.AppGroupPerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.databinding.model.AddAppGroupViewModel;
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

        new AsyncTask<Void, Void, Void>() {

            private Dialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                dialog = DialogHelper.newLoadingDialog(PackageStatePerspective.this);
                dialog.show();
            }

            @Override
            protected Void doInBackground(Void... params) {
                // loading application assets takes the most time
                preloadPackageAssets();
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setupView();
                dialog.dismiss();
            }
        }.execute();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (applicationStateViewModel == null) {
            return;
        }
        final boolean showSystemApps = viewOptionConfigDataAccessor.showSystemApps();
        applicationStateViewModel.updateShowSystemApps(showSystemApps);
    }

    private void setupView() {
        final PerspectiveStateActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_state_activity);

        final boolean showSystemApps = viewOptionConfigDataAccessor.showSystemApps();

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
                appStateProvider, packageStateUpdateEventManager,
                DialogHelper.newProgressDialog(PackageStatePerspective.this), appLauncher,
                showSystemApps);
        binding.setViewModel(applicationStateViewModel);

        AddAppGroupViewModel addAppGroupViewModel = new AddAppGroupViewModel(appGroupManager,
                applicationStateViewModel.getSelectedPackageNamesSupplier(),
                applicationStateViewModel.getClearSelectedPackagesRunnable());
        PerspectiveStateAppGroupDialogBinding appGroupDialogBinding = DataBindingUtil
                .inflate(LayoutInflater.from(PackageStatePerspective.this),
                        R.layout.perspective_state_app_group_dialog, null, false);
        appGroupDialogBinding.setAddAppGroupViewModel(addAppGroupViewModel);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(R.string.app_group_dialog_title);
        dialogBuilder.setPositiveButton(R.string.app_group_dialog_positive_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                addAppGroupViewModel.addPackageNamesToAppGroup();
            }
        });
        dialogBuilder.setNegativeButton(R.string.app_group_dialog_negative_button, null);
        dialogBuilder.setView(appGroupDialogBinding.getRoot());
        applicationStateViewModel.setAddToAppGroupDialog(dialogBuilder.create());
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
