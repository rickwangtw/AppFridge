package com.mysticwind.disabledappmanager;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public class CachedAppInfo {
        public String packageName;
        public String appName;
        public Drawable icon;
        public ApplicationInfo appInfo;
    }

    private Map<Integer, CachedAppInfo> positionToViewMap = new HashMap<>();

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        CachedAppInfo cachedAppInfoForPosition = positionToViewMap.get(position);
        if (cachedAppInfoForPosition == null) {
            if (view == null) {
                view = layoutInflater.inflate(R.layout.applistitem, null);
            }
            cachedAppInfoForPosition = newViewHolder(view, position);
            positionToViewMap.put(position, cachedAppInfoForPosition);
        }

        TextView textView = (TextView) view.findViewById(R.id.packagename);;
        textView.setText(cachedAppInfoForPosition.appName);

        ImageView imageView = (ImageView) view.findViewById(R.id.appicon);
        imageView.setImageDrawable(cachedAppInfoForPosition.icon);

        if (!cachedAppInfoForPosition.appInfo.enabled) {
            view.setBackgroundColor(Color.GRAY);
            view.setEnabled(false);
        } else {
            view.setBackgroundColor(Color.WHITE);
            view.setEnabled(true);
        }

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setChecked(
                appSelectedListener.isPackageNameSelected(cachedAppInfoForPosition.packageName));

        return view;
    }

    private CachedAppInfo newViewHolder(View view, int position) {
        ApplicationInfo appInfo = appInfoList.get(position);

        final CachedAppInfo cachedAppInfo = new CachedAppInfo();
        cachedAppInfo.packageName = appInfo.packageName;
        cachedAppInfo.appName = appInfo.loadLabel(packageManager).toString();
        cachedAppInfo.appInfo = appInfo;
        try {
            cachedAppInfo.icon = packageManager.getApplicationIcon(cachedAppInfo.packageName).getCurrent();
        } catch (PackageManager.NameNotFoundException e) {
        }
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setTag(appInfo.packageName);
        checkBox.setOnCheckedChangeListener(appSelectedListener);
        return cachedAppInfo;
    }
}
