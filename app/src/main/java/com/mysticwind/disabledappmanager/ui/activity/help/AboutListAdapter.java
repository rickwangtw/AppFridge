package com.mysticwind.disabledappmanager.ui.activity.help;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.mysticwind.disabledappmanager.R;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.Arrays;
import java.util.List;

public class AboutListAdapter extends BaseAdapter{
    private static final List<Integer> RESOURCE_ID_LIST = Arrays.asList(
            R.layout.help_about_author,
            R.layout.help_about_icon_designer,
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
                HtmlTextView textView = (HtmlTextView) convertView.findViewById(R.id.help_about_libraries_material_design_icons);
                textView.setHtmlFromString(
                        context.getResources().getString(R.string.help_about_libraries_material_design_icons),
                        new HtmlTextView.LocalImageGetter());
                return convertView;
            default:
                return null;
        }
    }
}
