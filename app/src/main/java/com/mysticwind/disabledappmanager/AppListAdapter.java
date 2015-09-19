package com.mysticwind.disabledappmanager;

import android.content.pm.ApplicationInfo;
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

import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class AppListAdapter extends BaseAdapter implements Observer {
    private static final String TAG = "AppListAdapter";

    private final AppStateProvider appStateProvider;
    private final AppNameProvider appNameProvider;
    private final AppIconProvider appIconProvider;
    private final List<ApplicationInfo> appInfoList;
    private final List<String> packageNameList;
    private final LayoutInflater layoutInflater;
    private final AppSelectedListener appSelectedListener;

    public AppListAdapter(AppStateProvider appStateProvider,
                          AppIconProvider appIconProvider,
                          AppNameProvider appNameProvider,
                          LayoutInflater layoutInflater,
                          List<ApplicationInfo> appInfoList,
                          AppSelectedListener appSelectedListener) {
        this.appStateProvider = appStateProvider;
        this.appNameProvider = appNameProvider;
        this.appIconProvider = appIconProvider;
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

        if (appStateProvider.isPackageEnabled(cachedAppInfoForPosition.packageName)) {
            view.setBackgroundColor(Color.WHITE);
            view.setEnabled(true);
        } else {
            view.setBackgroundColor(Color.GRAY);
            view.setEnabled(false);
        }

        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setTag(cachedAppInfoForPosition.packageName);
        checkBox.setChecked(
                appSelectedListener.isPackageNameSelected(cachedAppInfoForPosition.packageName));

        return view;
    }

    private CachedAppInfo newViewHolder(View view, int position) {
        ApplicationInfo appInfo = appInfoList.get(position);

        final CachedAppInfo cachedAppInfo = new CachedAppInfo();
        cachedAppInfo.packageName = appInfo.packageName;
        cachedAppInfo.appName = appNameProvider.getAppName(cachedAppInfo.packageName);
        cachedAppInfo.appInfo = appInfo;
        cachedAppInfo.icon = appIconProvider.getAppIcon(cachedAppInfo.packageName);
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(appSelectedListener);
        return cachedAppInfo;
    }

    @Override
    public void update(Observable observable, Object data) {
        notifyDataSetChanged();
    }

}
