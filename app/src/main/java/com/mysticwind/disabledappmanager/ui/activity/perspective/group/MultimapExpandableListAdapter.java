package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Lists.newArrayList;

public class MultimapExpandableListAdapter<T, K> extends BaseExpandableListAdapter {

    public interface ViewGenerator<Q, R> {
        View populateGroupView(Q object, View groupView, ViewGroup parent);
        View populateChildView(Q groupObject, R childObject, View childView, ViewGroup parent);
    }

    private final Comparator<T> groupComparator;
    private final Comparator<K> childComparator;
    private final Map<T, List<K>> keyToValueListMap = Maps.newHashMap();
    private final ViewGenerator<T, K> viewGenerator;

    private List<T> keyList;

    public MultimapExpandableListAdapter(final Multimap<T, K> multimap,
                                         final Comparator<T> groupComparator,
                                         final Comparator<K> childComparator,
                                         final ViewGenerator<T, K> viewGenerator) {
        this.groupComparator = Preconditions.checkNotNull(groupComparator);
        this.childComparator = Preconditions.checkNotNull(childComparator);
        updateKeyList(multimap.keys());

        for (T key : multimap.keySet()) {
            Collection<K> set = multimap.get(key);
            updateValueList(key, set);
        }
        this.viewGenerator = Preconditions.checkNotNull(viewGenerator);
    }

    private void updateKeyList(Collection<T> keyCollection) {
        final List<T> groupList = newArrayList(Sets.newHashSet(keyCollection));
        Collections.sort(groupList, groupComparator);
        this.keyList = groupList;
    }

    private void updateValueList(T key, Collection<K> valueCollection) {
        final List<K> valueList = newArrayList(Sets.newHashSet(valueCollection));
        Collections.sort(valueList, childComparator);
        keyToValueListMap.put(key, valueList);
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

    public void updateGroup(final T group, final Collection<K> values) {
        Set<T> keySet = Sets.newHashSet(this.keyList);
        keySet.add(group);
        updateKeyList(keySet);

        Set<K> valueSet = Sets.newHashSet(values);
        updateValueList(group, valueSet);

        notifyDataSetChanged();
    }

    public void removeGroup(final T group) {
        if (this.keyList.contains(group)) {
            this.keyList.remove(group);
        }
        if (keyToValueListMap.containsKey(group)) {
            keyToValueListMap.remove(group);
        }

        notifyDataSetChanged();
    }
}
