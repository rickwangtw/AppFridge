package com.mysticwind.library.widget.listview.expandable.adapter;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.mysticwind.library.search.model.FilterableFields;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java8.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static java8.util.stream.StreamSupport.stream;

public class MultimapExpandableListAdapter<T, K> extends BaseExpandableListAdapter implements Filterable {

    public interface ViewGenerator<Q, R> {
        View populateGroupView(Q object, View groupView, ViewGroup parent);
        View populateChildView(Q groupObject, R childObject, View childView, ViewGroup parent);
    }

    private final Comparator<T> groupComparator;
    private final Comparator<K> childComparator;
    private final Map<T, List<K>> originalKeyToValueListMap = Maps.newHashMap();
    private final ViewGenerator<T, K> viewGenerator;

    private List<T> originalKeyList;

    private boolean inFilterMode = false;
    private final Map<T, List<K>> filteredKeyToValueListMap = new HashMap<T, List<K>>();
    private List<T> filteredKeyList = Lists.newArrayList();
    private String cachedSearchQuery = "";


    public MultimapExpandableListAdapter(final Multimap<T, K> multimap,
                                         final Comparator<T> groupComparator,
                                         final Comparator<K> childComparator,
                                         final ViewGenerator<T, K> viewGenerator) {
        this.groupComparator = Preconditions.checkNotNull(groupComparator);
        this.childComparator = Preconditions.checkNotNull(childComparator);
        updateKeysAndValues(multimap);
        this.viewGenerator = Preconditions.checkNotNull(viewGenerator);
    }

    private void updateKeysAndValues(final Multimap<T, K> multimap) {
        updateKeyList(multimap.keys());

        for (T key : multimap.keySet()) {
            Collection<K> set = multimap.get(key);
            updateValueList(key, set);
        }
    }

    private void updateKeyList(Collection<T> keyCollection) {
        final List<T> groupList = newArrayList(Sets.newHashSet(keyCollection));
        Collections.sort(groupList, groupComparator);
        this.originalKeyList = groupList;
    }

    private void updateValueList(T key, Collection<K> valueCollection) {
        final List<K> valueList = newArrayList(Sets.newHashSet(valueCollection));
        Collections.sort(valueList, childComparator);
        originalKeyToValueListMap.put(key, valueList);
    }

    @Override
    public int getGroupCount() {
        return getOriginalKeyList().size();
    }

    private List<T> getOriginalKeyList() {
        if (inFilterMode) {
            return filteredKeyList;
        } else {
            return originalKeyList;
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        T key = getGroupFromGroupPosition(groupPosition);
        return getKeyToValueListMap().get(key).size();
    }

    private Map<T, List<K>> getKeyToValueListMap() {
        if (inFilterMode) {
            return filteredKeyToValueListMap;
        } else {
            return originalKeyToValueListMap;
        }
    }

    private T getGroupFromGroupPosition(int groupPosition) {
        return getOriginalKeyList().get(groupPosition);
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
        return getKeyToValueListMap().get(key).get(childPosition);
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
        Set<T> keySet = Sets.newHashSet(this.originalKeyList);
        keySet.add(group);
        updateKeyList(keySet);

        Set<K> valueSet = Sets.newHashSet(values);
        updateValueList(group, valueSet);

        if (inFilterMode) {
            getFilter().filter(cachedSearchQuery);
        }

        notifyDataSetChanged();
    }

    public void removeGroup(final T group) {
        if (this.originalKeyList.contains(group)) {
            this.originalKeyList.remove(group);
        }
        if (originalKeyToValueListMap.containsKey(group)) {
            originalKeyToValueListMap.remove(group);
        }

        if (inFilterMode) {
            getFilter().filter(cachedSearchQuery);
        }

        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            class GroupAndChildFilterResults extends FilterResults {
                Map<T, List<K>> filteredKeyToValueListMap;
                List<T> filteredKeyList;
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                if (constraint == null || constraint.length() == 0) {
                    return null;
                }
                cachedSearchQuery = constraint.toString().toLowerCase();
                final Map<T, List<K>> filteredKeyToValueListMap = Maps.newHashMap();
                final List<T> filteredKeyList = Lists.newArrayList();
                for (final T group : originalKeyList) {
                    final List<K> childList = originalKeyToValueListMap.get(group);
                    List<K> filteredChildList = stream(childList)
                            .filter(child -> isHit(child, cachedSearchQuery))
                            .collect(Collectors.toList());
                    if (!filteredChildList.isEmpty() || isHit(group, cachedSearchQuery)) {
                        filteredKeyList.add(group);
                        filteredKeyToValueListMap.put(group, filteredChildList);
                    }
                }
                GroupAndChildFilterResults filteredResults = new GroupAndChildFilterResults();
                filteredResults.filteredKeyList = filteredKeyList;
                filteredResults.filteredKeyToValueListMap = filteredKeyToValueListMap;
                return filteredResults;
            }

            private boolean isHit(final Object object, final String searchString) {
                final FilterableFields filterableFields;
                try {
                    filterableFields = (FilterableFields) object;
                } catch (ClassCastException e) {
                    return false;
                }
                return stream(filterableFields.getSearchableStringsOrderedByPriority())
                        .filter(string -> string.toLowerCase().contains(searchString))
                        .findAny()
                        .isPresent();
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                GroupAndChildFilterResults groupAndChildFilteredResults = (GroupAndChildFilterResults) results;
                if (results == null) {
                    if (!inFilterMode) {
                        return;
                    }
                    filteredKeyToValueListMap.clear();
                    inFilterMode = false;
                    filteredKeyList.clear();
                } else {
                    filteredKeyToValueListMap.clear();
                    filteredKeyToValueListMap.putAll(groupAndChildFilteredResults.filteredKeyToValueListMap);
                    filteredKeyList.clear();
                    filteredKeyList.addAll(groupAndChildFilteredResults.filteredKeyList);
                    inFilterMode = true;
                }
                notifyDataSetChanged();
            }
        };
    }

    public void updateDataSet(Multimap<T, K> updatedMultimap) {
        updateKeysAndValues(updatedMultimap);

        notifyDataSetChanged();
    }
}
