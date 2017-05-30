package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.TreeMultimap;

import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

public class TreeMultimapExpandableListAdapter<T, K> extends BaseExpandableListAdapter {

    public interface ViewGenerator<Q, R> {
        View populateGroupView(Q object, View groupView, ViewGroup parent);
        View populateChildView(Q groupObject, R childObject, View childView, ViewGroup parent);
    }

    private final Map<T, List<K>> keyToValueListMap = Maps.newHashMap();
    private final List<T> keyList;
    private final ViewGenerator<T, K> viewGenerator;

    public TreeMultimapExpandableListAdapter(final TreeMultimap<T, K> treeMultimap,
                                             final ViewGenerator<T, K> viewGenerator) {
        this.keyList = Lists.newArrayList(treeMultimap.keySet());
        for (T key : treeMultimap.keySet()) {
            NavigableSet<K> set = treeMultimap.get(key);
            keyToValueListMap.put(key, Lists.newArrayList(set));
        }
        this.viewGenerator = Preconditions.checkNotNull(viewGenerator);
    }

    @Override
    public int getGroupCount() {
        return keyList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        T key = getGroupFromGroupPosition(groupPosition);
        return keyToValueListMap.get(key).size();
    }

    private T getGroupFromGroupPosition(int groupPosition) {
        return keyList.get(groupPosition);
    }

    @Override
    public Object getGroup(int groupPosition) {
        return getGroupFromGroupPosition(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getChildFromPositions(groupPosition, childPosition);
    }

    private K getChildFromPositions(int groupPosition, int childPosition) {
        T key = getGroupFromGroupPosition(groupPosition);
        return keyToValueListMap.get(key).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return getGroupFromGroupPosition(groupPosition).hashCode();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChildFromPositions(groupPosition, childPosition).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(final int groupPosition,
                             final boolean isExpanded,
                             final View convertView,
                             final ViewGroup parent) {
        T groupObject = getGroupFromGroupPosition(groupPosition);
        return viewGenerator.populateGroupView(groupObject, convertView, parent);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        T groupObject = getGroupFromGroupPosition(groupPosition);
        K childObject = getChildFromPositions(groupPosition, childPosition);
        return viewGenerator.populateChildView(groupObject, childObject, convertView, parent);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
