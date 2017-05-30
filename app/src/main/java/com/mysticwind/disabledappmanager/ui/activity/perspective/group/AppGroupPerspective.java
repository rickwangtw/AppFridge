package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.TreeMultimap;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupActivityBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupAppItemBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupGroupItemBinding;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.ui.activity.perspective.PerspectiveBase;
import com.mysticwind.disabledappmanager.ui.activity.perspective.state.PackageStatePerspective_;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.databinding.model.AppGroupViewModel;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;

import org.androidannotations.annotations.EActivity;

import java.util.List;
import java.util.Map;
import java.util.Set;

import java8.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
@EActivity
public class AppGroupPerspective extends PerspectiveBase {

    private String allAppGroupName;
    private Map<String, ApplicationModel> packageNameToApplicationModelMap;
    // prevent reference release as the event managers are handling it using weak reference
    private AppAssetUpdateListener appAssetUpdateListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.allAppGroupName = getResources().getString(R.string.generated_app_group_name_all);
        final LayoutInflater layoutInflator = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        PerspectiveAppgroupActivityBinding binding = DataBindingUtil.setContentView(this, R.layout.perspective_appgroup_activity);

        final TreeMultimap<AppGroupViewModel, ApplicationModel> appGroupToApplicationModelMultimap =
                buildApplicationGroupToApplicationModelMultimap();

        final TreeMultimapExpandableListAdapter.ViewGenerator<AppGroupViewModel, ApplicationModel> databindingViewGenerator =
                new TreeMultimapExpandableListAdapter.ViewGenerator<AppGroupViewModel, ApplicationModel>() {
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
        TreeMultimapExpandableListAdapter<AppGroupViewModel, ApplicationModel> adapter =
                new TreeMultimapExpandableListAdapter<>(
                        appGroupToApplicationModelMultimap, databindingViewGenerator);
        binding.appGroupListView.setAdapter(adapter);
    }

    private TreeMultimap<AppGroupViewModel, ApplicationModel> buildApplicationGroupToApplicationModelMultimap() {
        packageNameToApplicationModelMap = stream(packageListProvider.getOrderedPackages())
                .map(appInfo -> getApplicationModel(appInfo))
                .collect(
                        Collectors.toMap(
                                applicationModel -> applicationModel.getPackageName(),
                                applicationModel -> applicationModel));

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

        TreeMultimap<AppGroupViewModel, ApplicationModel> appGroupToApplicationModelMultiMap = TreeMultimap.create(
                (appGroupViewModel1, appGroupViewModel2) -> appGroupViewModel1.getAppGroupName().compareTo(appGroupViewModel2.getAppGroupName()),
                (applicationModel1, applicationModel2) -> applicationModel1.getApplicationLabel().compareTo(applicationModel2.getApplicationLabel())
        );
        stream(appGroupManager.getAllAppGroups())
                .map(appGroupName -> new AppGroupViewModel(appGroupName, isVirtualAppGroup(appGroupName)))
                .forEach(appGroupViewModel -> {
                    Set<String> packageNames = appGroupManager.getPackagesOfAppGroup(appGroupViewModel.getAppGroupName());
                    List<ApplicationModel> applicationModelList = stream(packageNameToApplicationModelMap.entrySet())
                            .filter(entry -> packageNames.contains(entry.getKey()))
                            .map(entry -> entry.getValue())
                            .collect(Collectors.toList());

                    appGroupToApplicationModelMultiMap.putAll(appGroupViewModel, applicationModelList);
                });
        // add the virtual app group - all
        appGroupToApplicationModelMultiMap.putAll(new AppGroupViewModel(allAppGroupName, true),
                packageNameToApplicationModelMap.values());
        return appGroupToApplicationModelMultiMap;
    }

    private ApplicationModel getApplicationModel(final AppInfo appInfo) {
        final String packageName = appInfo.getPackageName();
        final boolean isEnabled = appInfo.isEnabled();

        return ApplicationModel.builder()
                .packageName(packageName)
                .applicationAssetSupplier(() -> packageAssetService.getPackageAssets(packageName))
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
                DialogHelper.newNewAppGroupDialog(this, packageListProvider, appIconProvider,
                        appNameProvider, appGroupManager).show();
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
