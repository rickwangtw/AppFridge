package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupActivityBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupAppItemBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupGroupItemBinding;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupOperation;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdate;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.domain.state.PackageState;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdate;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateListener;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.common.PackageStateUpdateAsyncTask;
import com.mysticwind.disabledappmanager.ui.databinding.model.AppGroupViewModel;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;
import com.mysticwind.library.widget.listview.expandable.adapter.MultimapExpandableListAdapter;

import org.androidannotations.annotations.EActivity;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;

import java8.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
@EActivity
public class AppGroupPerspective extends PerspectiveBase {

    private static final Comparator<ApplicationModel> APPLICATION_MODEL_COMPARATOR =
            (applicationModel1, applicationModel2) -> applicationModel1.getApplicationLabel().compareTo(applicationModel2.getApplicationLabel());

    private String allAppGroupName = "";
    private Map<String, ApplicationModel> packageNameToApplicationModelMap;
    // prevent reference release as the event managers are handling it using weak reference
    private AppAssetUpdateListener appAssetUpdateListener;
    private AppGroupUpdateListener appGroupUpdateListener;
    private ExpandableListView expandableListView;
    private MultimapExpandableListAdapter multimapExpandableListAdapter;
    private PackageStateUpdateListener packageStateUpdateListener;
    private boolean enteredSearchMode = false;

    private final Comparator<AppGroupViewModel> appGroupViewModelComparator =
            (appGroupViewModel1, appGroupViewModel2) -> {
                if (allAppGroupName.equals(appGroupViewModel1.getAppGroupName())) {
                    return 1;
                } else if (allAppGroupName.equals(appGroupViewModel2.getAppGroupName())) {
                    return -1;
                }
                return appGroupViewModel1.getAppGroupName().compareTo(appGroupViewModel2.getAppGroupName());
            };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new AsyncTask<Void, Void, Void>() {

            private Dialog dialog;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                dialog = DialogHelper.newLoadingDialog(AppGroupPerspective.this);
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

    private void setupView() {
        this.allAppGroupName = getResources().getString(R.string.generated_app_group_name_all);
        final LayoutInflater layoutInflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        PerspectiveAppgroupActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_appgroup_activity);
        expandableListView = binding.appGroupListView;

        final Multimap<AppGroupViewModel, ApplicationModel> appGroupToApplicationModelMultimap =
                buildApplicationGroupToApplicationModelMultimap();

        final MultimapExpandableListAdapter.ViewGenerator<AppGroupViewModel, ApplicationModel> viewGenerator =
                new MultimapExpandableListAdapter.ViewGenerator<AppGroupViewModel, ApplicationModel>() {
                    @Override
                    public View populateGroupView(AppGroupViewModel appGroupViewModel, View groupView, ViewGroup parent) {
                        if (groupView == null) {
                            groupView = layoutInflator.inflate(R.layout.perspective_appgroup_group_item, null);
                        }
                        PerspectiveAppgroupGroupItemBinding groupBinding = DataBindingUtil.bind(groupView);
                        groupBinding.setAppGroup(appGroupViewModel);
                        groupBinding.executePendingBindings();
                        return groupView;
                    }

                    @Override
                    public View populateChildView(AppGroupViewModel appGroupViewModel, ApplicationModel applicationModel, View childView, ViewGroup parent) {
                        if (childView == null) {
                            childView = layoutInflator.inflate(R.layout.perspective_appgroup_app_item, null);
                        }
                        final PerspectiveAppgroupAppItemBinding appBinding = DataBindingUtil.bind(childView);

                        appBinding.setAppGroupName(appGroupViewModel.getAppGroupName());
                        appBinding.setApplication(applicationModel);
                        appBinding.setIsWithinVirtualAppGroup(isVirtualAppGroup(appGroupViewModel.getAppGroupName()));
                        appBinding.executePendingBindings();
                        return childView;
                    }
                };
        this.multimapExpandableListAdapter =
                new MultimapExpandableListAdapter<>(appGroupToApplicationModelMultimap,
                        appGroupViewModelComparator, APPLICATION_MODEL_COMPARATOR, viewGenerator);
        binding.appGroupListView.setAdapter(this.multimapExpandableListAdapter);

        appAssetUpdateListener = new AppAssetUpdateListener() {
            @Override
            public void update(AppAssetUpdate event) {
                final ApplicationModel applicationModel = packageNameToApplicationModelMap.get(event.getPackageName());
                if (applicationModel == null) {
                    return;
                }
                PackageAssets packageAssets = packageAssetService.getPackageAssets(event.getPackageName());
                applicationModel.setPackageAssets(packageAssets);
            }
        };

        appAssetUpdateEventManager.registerListener(appAssetUpdateListener);


        appGroupUpdateListener = new AppGroupUpdateListener() {
            @Override
            public void update(AppGroupUpdate event) {
                if (multimapExpandableListAdapter == null) {
                    return;
                }
                final String appGroupName = event.getAppGroupName();
                final AppGroupViewModel appGroupViewModel = getAppGroupViewModel(appGroupName);
                if (AppGroupOperation.DELETE == event.getOperation()) {
                    multimapExpandableListAdapter.removeGroup(appGroupViewModel);
                } else {
                    Multimap<AppGroupViewModel, ApplicationModel> updatedMultimap = buildApplicationGroupToApplicationModelMultimap();
                    Collection<ApplicationModel> applicationModels = updatedMultimap.get(appGroupViewModel);
                    multimapExpandableListAdapter.updateGroup(appGroupViewModel, applicationModels);
                }
            }
        };
        appGroupUpdateEventManager.registerListener(appGroupUpdateListener);

        packageStateUpdateListener = new PackageStateUpdateListener() {
            @Override
            public void update(PackageStateUpdate event) {
                final String packageName = event.getAppGroupName();
                final boolean isEnabled = appStateProvider.isPackageEnabled(packageName);
                ApplicationModel applicationModel = packageNameToApplicationModelMap.get(packageName);
                if (applicationModel != null) {
                    applicationModel.setEnabled(isEnabled);
                }
            }
        };
        packageStateUpdateEventManager.registerListener(packageStateUpdateListener);
    }

    private Multimap<AppGroupViewModel, ApplicationModel> buildApplicationGroupToApplicationModelMultimap() {
        packageNameToApplicationModelMap = stream(packageListProvider.getPackages())
                .map(appInfo -> getApplicationModel(appInfo))
                .collect(
                        Collectors.toMap(
                                applicationModel -> applicationModel.getPackageName(),
                                applicationModel -> applicationModel));

        Multimap<AppGroupViewModel, ApplicationModel> appGroupToApplicationModelMultiMap = ArrayListMultimap.create();
        stream(appGroupManager.getAllAppGroups())
                .map(appGroupName -> getAppGroupViewModel(appGroupName))
                .forEach(appGroupViewModel -> {
                    Set<String> packageNames = appGroupManager.getPackagesOfAppGroup(appGroupViewModel.getAppGroupName());
                    Set<ApplicationModel> applicationModelList = stream(packageNameToApplicationModelMap.entrySet())
                            .filter(entry -> packageNames.contains(entry.getKey()))
                            .map(entry -> entry.getValue())
                            .collect(Collectors.toSet());

                    appGroupToApplicationModelMultiMap.putAll(appGroupViewModel, applicationModelList);
                });
        // add the virtual app group - all
        appGroupToApplicationModelMultiMap.putAll(getVirtualAllAppGroupViewModel(), packageNameToApplicationModelMap.values());
        return appGroupToApplicationModelMultiMap;
    }

    private AppGroupViewModel getVirtualAllAppGroupViewModel() {
        return AppGroupViewModel.builder()
                .appGroupName(allAppGroupName)
                .isVirtualGroup(true)
                .build();
    }

    private AppGroupViewModel getAppGroupViewModel(final String appGroupName) {
        return AppGroupViewModel.builder()
                .appGroupName(appGroupName)
                .isVirtualGroup(isVirtualAppGroup(appGroupName))
                .appGroupPackageFreezingConsumer(appGroup -> {
                    Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroup);
                    log.debug("Disable apps: " + packages);
                    notifyManualStateUpdate(packages, false);
                    new PackageStateUpdateAsyncTask(packageStateController, appStateProvider,
                            packages, PackageStateUpdateAsyncTask.Action.DISABLE)
                            .withProgressDialog(DialogHelper.newProgressDialog(AppGroupPerspective.this))
                            .withEndingToast(Toast.makeText(AppGroupPerspective.this,
                                    getResources().getString(R.string.toast_disabled_packages_msg_prefix) + " " + packages,
                                    Toast.LENGTH_SHORT))
                            .execute();
                })
                .appGroupPackageUnfreezingConsumer(appGroup -> {
                    Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroup);
                    log.debug("Enable apps: " + packages);
                    notifyManualStateUpdate(packages, true);
                    new PackageStateUpdateAsyncTask(packageStateController, appStateProvider, packages,
                            PackageStateUpdateAsyncTask.Action.ENABLE)
                            .withProgressDialog(DialogHelper.newProgressDialog(AppGroupPerspective.this))
                            .withEndingToast(Toast.makeText(AppGroupPerspective.this,
                                    getResources().getString(R.string.toast_enabled_packages_msg_prefix) + " " + packages,
                                    Toast.LENGTH_SHORT))
                            .execute();
                })
                .appGroupPackageAddingConsumer(
                        appGroup ->
                                DialogHelper.newPackageListForAddingToGroupDialog(this, appGroupName,
                                        packageListProvider, packageAssetService, appGroupManager).show()
                )
                .appGroupDeletingConsumer(
                        appGroup ->
                                DialogHelper.newConfirmDeleteAppGroupDialog(this, appGroupName, appGroupManager).show()
                )
                .build();
    }

    private void notifyManualStateUpdate(final String packageName,
                                         final boolean enable) {
        notifyManualStateUpdate(Sets.newHashSet(packageName), enable);
    }

    private void notifyManualStateUpdate(final Collection<String> packageNames,
                                         final boolean enable) {
        PackageState packageState = enable ? PackageState.ENABLE : PackageState.DISABLE;
        for (String packageName : Sets.newHashSet(packageNames)) {
            manualStateUpdateEventManager.publishUpdate(packageName, packageState);
        }
    }

    private ApplicationModel getApplicationModel(final AppInfo appInfo) {
        final String packageName = appInfo.getPackageName();
        final boolean isEnabled = appInfo.isEnabled();
        final PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
        return ApplicationModel.builder()
                .packageName(packageName)
                .packageStatusChangeConsumer((packageToChangeState, newState) -> {
                    final Toast toast;
                    final PackageStateUpdateAsyncTask.Action action = newState ?
                            PackageStateUpdateAsyncTask.Action.ENABLE : PackageStateUpdateAsyncTask.Action.DISABLE;
                    if (newState) {
                        String enablingToastMessagePrefix = getResources().getString(R.string.toast_enabled_packages_msg_prefix);
                        toast = Toast.makeText(this, enablingToastMessagePrefix + " " + packageName, Toast.LENGTH_SHORT);
                    } else {
                        String disablingToastMessagePrefix = getResources().getString(R.string.toast_disabled_packages_msg_prefix);
                        toast = Toast.makeText(this, disablingToastMessagePrefix + " " + packageName, Toast.LENGTH_SHORT);
                    }

                    notifyManualStateUpdate(packageName, newState);
                    new PackageStateUpdateAsyncTask(
                            packageStateController,
                            appStateProvider,
                            Arrays.asList(packageName),
                            action
                    ).withEndingToast(toast).execute();
                })
                .appGroupPackageRemovingConsumer(
                        (appGroupName, packageNameToRemove) ->
                                DialogHelper.newConfirmDeletePackageFromAppGroupDialog(this,
                                        packageNameToRemove, appGroupName, appGroupManager).show())
                .applicationLabel(packageAssets.getAppName())
                .applicationIcon(packageAssets.getIconDrawable())
                .isEnabled(isEnabled)
                .applicationLauncher(
                        name -> appLauncher.launch(this, name))
                .build();
    }

    private boolean isVirtualAppGroup(final String appGroupName) {
        if (allAppGroupName.equals(appGroupName)) {
            return true;
        } else {
            return false;
        }
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
                DialogHelper.newNewAppGroupDialog(this, packageListProvider, packageAssetService, appGroupManager).show();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void performSearch(String searchQuery) {
        if (Strings.isNullOrEmpty(searchQuery)) {
            cancelSearch();
            return;
        }
        enteredSearchMode = true;
        multimapExpandableListAdapter.getFilter().filter(searchQuery);
        expandAllGroups();
    }

    private void expandAllGroups() {
        if (expandableListView == null) {
            return;
        }
        for (int groupIndex = 0 ; groupIndex <multimapExpandableListAdapter.getGroupCount() ; ++groupIndex) {
            expandableListView.expandGroup(groupIndex);
        }
    }

    @Override
    protected void cancelSearch() {
        if (enteredSearchMode) {
            multimapExpandableListAdapter.getFilter().filter(null);
            collapseAllGroups();
        }
        enteredSearchMode = false;
    }

    private void collapseAllGroups() {
        if (expandableListView == null) {
            return;
        }
        for (int groupIndex = 0 ; groupIndex <multimapExpandableListAdapter.getGroupCount() ; ++groupIndex) {
            expandableListView.collapseGroup(groupIndex);
        }
    }
}
