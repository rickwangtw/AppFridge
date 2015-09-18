package com.mysticwind.disabledappmanager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class AppListAdapter extends BaseAdapter {
    private static final String TAG = "AppListAdapter";

    private final List<ApplicationInfo> appInfoList;
    private final List<String> packageNameList;
    private final LayoutInflater layoutInflater;
    private final PackageManager packageManager;
    private final AppSelectedListener appSelectedListener;

    public AppListAdapter(PackageManager packageManager,
                          LayoutInflater layoutInflater,
                          List<ApplicationInfo> appInfoList,
                          AppSelectedListener appSelectedListener) {
        this.packageManager = packageManager;
        this.layoutInflater = layoutInflater;
        this.appInfoList = appInfoList;
        this.appSelectedListener = appSelectedListener;

        this.packageNameList = new ArrayList<String>(appInfoList.size());
        for (ApplicationInfo appInfo : appInfoList){
            this.packageNameList.add(appInfo.packageName);
        }
        Log.i(TAG, "Size of packages: " + appInfoList.size());
    }

    public int getCount() {
        return appInfoList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public class ViewHolder {
        public String packageName;
        public String appName;
        public TextView textView;
        public ImageView imageView;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        final ViewHolder holder;
        ApplicationInfo appInfo = appInfoList.get(position);
        if(convertView == null) {
            view = layoutInflater.inflate(R.layout.applistitem, null);
            holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.packagename);;
            holder.imageView = (ImageView) view.findViewById(R.id.appicon);
            CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
            checkBox.setTag(appInfo.packageName);
            checkBox.setOnCheckedChangeListener(appSelectedListener);
            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }

        holder.packageName = appInfo.packageName;
        holder.appName = appInfo.loadLabel(packageManager).toString();
        holder.textView.setText(holder.appName);
        if (!appInfo.enabled) {
            view.setBackgroundColor(Color.GRAY);
            view.setEnabled(false);
        }
        try {
            holder.imageView.setImageDrawable(packageManager.getApplicationIcon(holder.packageName).getCurrent());
        } catch (PackageManager.NameNotFoundException e) {
        }
        return view;
    }
}
