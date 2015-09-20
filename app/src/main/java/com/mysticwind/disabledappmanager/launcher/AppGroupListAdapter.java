package com.mysticwind.disabledappmanager.launcher;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppGroupListAdapter extends BaseExpandableListAdapter {
    private final AppGroupManager appGroupManager;
    private final LayoutInflater layoutInflator;
    private final AppIconProvider appIconProvider;
    private final AppNameProvider appNameProvider;
    private final List<String> allAppGroups;
    private final Map<String, List<String>> appGroupToPackageListMap = new HashMap<>();

    public AppGroupListAdapter(AppGroupManager appGroupManager, AppIconProvider appIconProvider,
                               AppNameProvider appNameProvider, LayoutInflater layoutInflator) {
        this.appGroupManager = appGroupManager;
        this.appIconProvider = appIconProvider;
        this.appNameProvider = appNameProvider;
        this.layoutInflator = layoutInflator;
        this.allAppGroups = getSortedAllAppGroups();
    }

    private List<String> getSortedAllAppGroups() {
        List<String> allAppGroups = new ArrayList<String>(appGroupManager.getAllAppGroups());
        Collections.sort(allAppGroups);
        return allAppGroups;
    }

    private String getAppGroup(int groupPosition) {
        String appGroup = allAppGroups.get(groupPosition);
        return appGroup;
    }

    private List<String> getPackageListOfGroupPosition(int groupPosition) {
        String appGroup = getAppGroup(groupPosition);
        List<String> packageNameList = appGroupToPackageListMap.get(appGroup);
        if (packageNameList == null) {
            packageNameList = new ArrayList<>(appGroupManager.getPackagesOfAppGroup(appGroup));
            Collections.sort(packageNameList);
            appGroupToPackageListMap.put(appGroup, packageNameList);
        }
        return packageNameList;
    }

    private String getPackageNameOfGroupPositionChildPosition(int groupPosition, int childPosition) {
        return getPackageListOfGroupPosition(groupPosition).get(childPosition);
    }

    @Override
    public int getGroupCount() {
        return allAppGroups.size();
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
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(android.R.layout.simple_dropdown_item_1line, null);
        }
        String appGroup = getAppGroup(groupPosition);
        ((TextView) convertView).setText(appGroup);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(R.layout.launcher_app_item, null);
        }
        String packageName
                = getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);

        ImageView iconView = (ImageView) convertView.findViewById(R.id.appicon);
        TextView packageNameTextView = (TextView) convertView.findViewById(R.id.packagename);

        iconView.setImageDrawable(appIconProvider.getAppIcon(packageName));
        packageNameTextView.setText(appNameProvider.getAppName(packageName));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
