package com.mysticwind.disabledappmanager.ui.activity.help;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.mysticwind.disabledappmanager.R;

import java.util.Arrays;
import java.util.List;

import lombok.Value;

public class AboutListAdapter extends BaseAdapter{
    private static final List<Integer> RESOURCE_ID_LIST = Arrays.asList(
            R.layout.help_about_author,
            R.layout.help_about_icon_designer,
            R.layout.help_about_website,
            R.layout.help_about_libraries);

    @Value
    static class Library {
        int descriptionResourceId;
        int licenseResourceId;
        int urlResourceId;
    }

    private static final List<Library> LIBRARIES = ImmutableList.of(
            new Library(R.string.help_about_libraries_material_design_icons_description, R.string.help_about_libraries_material_design_icons_license, R.string.help_about_libraries_material_design_url),
            new Library(R.string.help_about_libraries_acacia_description, R.string.help_about_libraries_acacia_license, R.string.help_about_libraries_acacia_url),
            new Library(R.string.help_about_libraries_eventbus_description, R.string.help_about_libraries_eventbus_license, R.string.help_about_libraries_eventbus_url),
            new Library(R.string.help_about_libraries_dagger2_description, R.string.help_about_libraries_dagger2_license, R.string.help_about_libraries_dagger2_url),
            new Library(R.string.help_about_libraries_lombok_description, R.string.help_about_libraries_lombok_license, R.string.help_about_libraries_lombok_url),
            new Library(R.string.help_about_libraries_guava_description, R.string.help_about_libraries_guava_license, R.string.help_about_libraries_guava_url),
            new Library(R.string.help_about_libraries_logback_description, R.string.help_about_libraries_logback_license, R.string.help_about_libraries_logback_url),
            new Library(R.string.help_about_libraries_android_annotations_description, R.string.help_about_libraries_android_annotations_license, R.string.help_about_libraries_android_annotations_url),
            new Library(R.string.help_about_libraries_gson_description, R.string.help_about_libraries_gson_license, R.string.help_about_libraries_gson_url),
            new Library(R.string.help_about_libraries_joda_time_android_description, R.string.help_about_libraries_joda_time_android_license, R.string.help_about_libraries_joda_time_android_url),
            new Library(R.string.help_about_libraries_libsuperuser_description, R.string.help_about_libraries_libsuperuser_license, R.string.help_about_libraries_libsuperuser_url),
            new Library(R.string.help_about_libraries_retrolambda_description, R.string.help_about_libraries_retrolambda_license, R.string.help_about_libraries_retrolambda_url),
            new Library(R.string.help_about_libraries_streamsupport_description, R.string.help_about_libraries_streamsupport_license, R.string.help_about_libraries_streamsupport_url),
            new Library(R.string.help_about_libraries_rxrecycleradapter_description, R.string.help_about_libraries_rxrecycleradapter_license, R.string.help_about_libraries_rxrecycleradapter_url),
            new Library(R.string.help_about_libraries_swipelayout_description, R.string.help_about_libraries_swipelayout_license, R.string.help_about_libraries_swipelayout_url)
    );

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
                final LinearLayout libraryViewLinearLayout = (LinearLayout) convertView;
                for (final Library library : LIBRARIES) {
                    View libraryItemLayout = layoutInflater.inflate(R.layout.help_about_libraries_item, libraryViewLinearLayout, false);
                    ((TextView) libraryItemLayout.findViewById(R.id.library_description)).setText(library.descriptionResourceId);
                    ((TextView) libraryItemLayout.findViewById(R.id.library_license)).setText(library.licenseResourceId);
                    setupButtonResourceIdWithLinkResourceId(libraryItemLayout, R.id.library_link_button, library.urlResourceId);
                    libraryViewLinearLayout.addView(libraryItemLayout);
                }
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
