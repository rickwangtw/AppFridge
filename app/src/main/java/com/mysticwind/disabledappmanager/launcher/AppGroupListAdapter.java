package com.mysticwind.disabledappmanager.launcher;

import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.mysticwind.disabledappmanager.domain.AppGroupManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppGroupListAdapter extends BaseExpandableListAdapter {
    private final AppGroupManager appGroupManager;
    private final LayoutInflater layoutInflator;
    private final List<String> allAppGroups;
    private final Map<String, List<String>> appGroupToPackageListMap = new HashMap<>();

    public AppGroupListAdapter(AppGroupManager appGroupManager, LayoutInflater layoutInflator) {
        this.appGroupManager = appGroupManager;
        this.layoutInflator = layoutInflator;

        List<String> allAppGroups = new ArrayList<String>(appGroupManager.getAllAppGroups());
        Collections.sort(allAppGroups);
        this.allAppGroups = allAppGroups;
    }

    @Override
    public int getGroupCount() {
        return allAppGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String appGroup = allAppGroups.get(groupPosition);
        List<String> packageNameList = appGroupToPackageListMap.get(appGroup);
        if (packageNameList == null) {
            packageNameList = new ArrayList<>(appGroupManager.getPackagesOfAppGroup(appGroup));
            Collections.sort(packageNameList);
            appGroupToPackageListMap.put(appGroup, packageNameList);
        }
        return packageNameList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return allAppGroups.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String appGroup = allAppGroups.get(groupPosition);
        List<String> packageNameList = appGroupToPackageListMap.get(appGroup);
        return packageNameList.get(childPosition);
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
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(android.R.layout.simple_dropdown_item_1line, null);
        }
        String appGroup = allAppGroups.get(groupPosition);
        ((TextView) convertView).setText(appGroup);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(android.R.layout.simple_dropdown_item_1line, null);
        }
        String appGroup = allAppGroups.get(groupPosition);
        List<String> packageNameList = appGroupToPackageListMap.get(appGroup);
        ((TextView) convertView).setText(packageNameList.get(childPosition));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
