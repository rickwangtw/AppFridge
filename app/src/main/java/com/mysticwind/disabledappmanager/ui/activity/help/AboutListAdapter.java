package com.mysticwind.disabledappmanager.ui.activity.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.mysticwind.disabledappmanager.R;

import java.util.Arrays;
import java.util.List;

public class AboutListAdapter extends BaseAdapter{
    private static final List<Integer> RESOURCE_ID_LIST = Arrays.asList(
            R.layout.help_about_author,
            R.layout.help_about_icon_designer,
            R.layout.help_about_website,
            R.layout.help_about_libraries);

    private final Context context;
    private final LayoutInflater layoutInflater;

    public AboutListAdapter(final Context context, final LayoutInflater layoutInflater) {
        this.context = context;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return RESOURCE_ID_LIST.size();
    }

    @Override
    public Object getItem(int position) {
        return RESOURCE_ID_LIST.get(position);
    }

    @Override
    public long getItemId(int position) {
        return RESOURCE_ID_LIST.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        switch (position) {
            case 0:
            case 1:
                if (convertView == null) {
                    convertView = layoutInflater.inflate(RESOURCE_ID_LIST.get(position), null);
                }
                return convertView;
            case 2:
                if (convertView == null) {
                    convertView = layoutInflater.inflate(RESOURCE_ID_LIST.get(position), null);
                }
                setupButtonResourceIdWithLinkResourceId(
                        convertView, R.id.website_link_button, R.string.help_about_website_url);
                return convertView;
            case 3:
                if (convertView == null) {
                    convertView = layoutInflater.inflate(RESOURCE_ID_LIST.get(position), null);
                }
                setupButtonResourceIdWithLinkResourceId(
                        convertView, R.id.material_design_icons_link_button,
                        R.string.help_about_libraries_material_design_url);
                setupButtonResourceIdWithLinkResourceId(
                        convertView, R.id.acacia_link_button,
                        R.string.help_about_libraries_acacia_url);
                setupButtonResourceIdWithLinkResourceId(
                        convertView, R.id.eventbus_link_button,
                        R.string.help_about_libraries_eventbus_url);
                return convertView;
            default:
                return null;
        }
    }

    private void setupButtonResourceIdWithLinkResourceId(
            View view, int buttonResourceId, final int linkResourceId) {
        view.findViewById(buttonResourceId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = context.getResources().getString(linkResourceId);
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            }
        });
    }
}