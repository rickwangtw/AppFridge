package com.mysticwind.disabledappmanager.ui.common;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StaticResourceAdapter extends BaseAdapter {
    private final LayoutInflater layoutInflater;
    private final List<Integer> resourceIds;

    public StaticResourceAdapter(LayoutInflater layoutInflater, Collection<Integer> resourceIds) {
        this.layoutInflater = layoutInflater;
        this.resourceIds = new ArrayList<>(resourceIds);
    }

    @Override
    public int getCount() {
        return resourceIds.size();
    }

    @Override
    public Object getItem(int position) {
        return resourceIds.get(position);
    }

    @Override
    public long getItemId(int position) {
        return resourceIds.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(resourceIds.get(position), null);
        }
        return convertView;
    }
}
