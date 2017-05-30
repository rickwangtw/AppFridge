package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupAppItemBinding;
import com.mysticwind.disabledappmanager.databinding.PerspectiveAppgroupGroupItemBinding;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.ui.databinding.model.AppGroupViewModel;
import com.mysticwind.disabledappmanager.ui.databinding.model.ApplicationModel;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import java8.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class AppGroupListAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final AppGroupManager appGroupManager;
    private final LayoutInflater layoutInflator;
    private final PackageAssetService packageAssetService;
    private final AppStateProvider appStateProvider;
    private final AppLauncher appLauncher;
    private final List<String> cachedAppGroupList;
    private final String allAppGroupName;
    private final LoadingCache<String, List<String>> packageListCache;

    public AppGroupListAdapter(final Context context,
                               final AppGroupManager appGroupManager,
                               final PackageAssetService packageAssetService,
                               final AppStateProvider appStateProvider,
                               final PackageListProvider packageListProvider,
                               final AppLauncher appLauncher,
                               final LayoutInflater layoutInflator) {
        this.context = context;
        this.appGroupManager = appGroupManager;
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.appStateProvider = appStateProvider;
        Preconditions.checkNotNull(packageListProvider);
        this.appLauncher = appLauncher;
        this.layoutInflator = layoutInflator;
        this.allAppGroupName = context.getResources().getString(R.string.generated_app_group_name_all);
        this.cachedAppGroupList = getPersistedAppGroupsSorted();

        this.packageListCache = CacheBuilder.newBuilder()
                .build(new CacheLoader<String, List<String>>() {
                    @Override
                    public List<String> load(String appGroupName) throws Exception {
                        if (allAppGroupName.equals(appGroupName)) {
                            return stream(packageListProvider.getOrderedPackages())
                                    .map(appInfo -> appInfo.getPackageName())
                                    .sorted()
                                    .collect(Collectors.toList());
                        } else {
                            return stream(appGroupManager.getPackagesOfAppGroup(appGroupName))
                                    .sorted()
                                    .collect(Collectors.toList());
                        }
                    }
                });
    }

    @Override
    public int getGroupCount() {
        return cachedAppGroupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<String> packageNameList = getPackageListOfGroupPosition(groupPosition);
        return packageNameList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return getAppGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getAppGroup(groupPosition).hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        String appGroup = getAppGroup(groupPosition);
        String packageName = getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);
        return Objects.hashCode(appGroup, packageName);
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(R.layout.perspective_appgroup_group_item, null);
        }
        PerspectiveAppgroupGroupItemBinding groupBinding = DataBindingUtil.bind(convertView);
        String appGroupName = getAppGroup(groupPosition);
        AppGroupViewModel appGroupViewModel = new AppGroupViewModel(appGroupName, isVirtualAppGroup(appGroupName));
        groupBinding.setAppGroup(appGroupViewModel);
        groupBinding.executePendingBindings();
        return convertView;
    }

    private boolean isVirtualAppGroup(final String appGroupName) {
        if (allAppGroupName.equals(appGroupName)) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(R.layout.perspective_appgroup_app_item, null);
        }
        final PerspectiveAppgroupAppItemBinding appBinding = DataBindingUtil.bind(convertView);

        final String appGroup = getAppGroup(groupPosition);
        appBinding.setAppGroupName(appGroup);

        final String packageName = getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);
        appBinding.setApplication(getApplicationModel(packageName));
        appBinding.setIsWithinVirtualAppGroup(isVirtualAppGroup(appGroup));
        appBinding.executePendingBindings();
        return convertView;
    }

    private ApplicationModel getApplicationModel(final String packageName) {
        return ApplicationModel.builder()
                .packageName(packageName)
                .applicationAssetSupplier(() -> packageAssetService.getPackageAssets(packageName))
                .isEnabled(appStateProvider.isPackageEnabled(packageName))
                .applicationLauncher(
                        name -> appLauncher.launch(context, name))
                .build();
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    private List<String> getPersistedAppGroupsSorted() {
        List<String> appGroups = stream(appGroupManager.getAllAppGroups())
                .sorted()
                .collect(Collectors.toList());
        appGroups.add(allAppGroupName);
        return appGroups;
    }

    private String getPackageNameOfGroupPositionChildPosition(int groupPosition, int childPosition) {
        return getPackageListOfGroupPosition(groupPosition).get(childPosition);
    }

    private List<String> getPackageListOfGroupPosition(int groupPosition) {
        String appGroup = getAppGroup(groupPosition);
        return getPackageListOfAppGroupName(appGroup);
    }

    private String getAppGroup(int groupPosition) {
        return cachedAppGroupList.get(groupPosition);
    }

    private List<String> getPackageListOfAppGroupName(String appGroupName) {
        try {
            return packageListCache.get(appGroupName);
        } catch (ExecutionException e) {
            log.error("Error getting package list from app group: " + appGroupName, e);
            return Collections.emptyList();
        }
    }

}
