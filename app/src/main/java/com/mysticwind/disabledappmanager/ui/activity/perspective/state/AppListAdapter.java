package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableList;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdate;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateListener;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import de.greenrobot.event.EventBus;

public class AppListAdapter extends BaseAdapter implements Observer {
    private static final String TAG = "AppListAdapter";

    private final Context context;
    private final PackageListProvider packageListProvider;
    private final AppStateProvider appStateProvider;
    private final AppNameProvider appNameProvider;
    private final AppIconProvider appIconProvider;
    private final AppLauncher appLauncher;
    private final LayoutInflater layoutInflater;
    private final AppSelectedListener appSelectedListener;
    private List<AppInfo> appInfoList;
    private List<AppInfo> searchFilteredAppInfoList;
    private boolean searchEnabled = false;

    private AppAssetUpdateListener appAssetUpdateListener = new AppAssetUpdateListener() {
        @Override
        public void update(AppAssetUpdate event) {
            notifyDataSetChanged();
        }
    };

    private PackageStateUpdateListener packageStateUpdateListener = new PackageStateUpdateListener() {
        @Override
        public void update(PackageStateUpdate event) {
            appInfoList = packageListProvider.getOrderedPackages();
            notifyDataSetChanged();
        }
    };

    public AppListAdapter(Context context, PackageListProvider packageListProvider, AppStateProvider appStateProvider,
                          AppIconProvider appIconProvider,
                          AppNameProvider appNameProvider,
                          AppLauncher appLauncher, LayoutInflater layoutInflater,
                          AppSelectedListener appSelectedListener) {
        this.context = context;
        this.packageListProvider = packageListProvider;
        this.appStateProvider = appStateProvider;
        this.appNameProvider = appNameProvider;
        this.appIconProvider = appIconProvider;
        this.appLauncher = appLauncher;
        this.layoutInflater = layoutInflater;
        this.appSelectedListener = appSelectedListener;

        this.appInfoList = packageListProvider.getOrderedPackages();
        Log.i(TAG, "Size of packages: " + appInfoList.size());
    }

    public int getCount() {
        if (searchEnabled) {
            return searchFilteredAppInfoList.size();
        }
        return appInfoList.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public void doSearch(String searchQuery) {
        ImmutableList.Builder<AppInfo> filteredAppInfoListBuilder = new ImmutableList.Builder<>();
        for (AppInfo appInfo : appInfoList) {
            String packageName = appInfo.getPackageName();
            String appName = appNameProvider.getAppName(packageName);

            if (matchesSearchQuery(searchQuery, appName, packageName)) {
                filteredAppInfoListBuilder.add(appInfo);
            }
        }
        this.searchFilteredAppInfoList = filteredAppInfoListBuilder.build();
        this.searchEnabled = true;
        notifyDataSetChanged();
    }

    private boolean matchesSearchQuery(String searchQuery, String appName, String packageName) {
        String lowerCaseSearchQuery = searchQuery.toLowerCase();
        if (packageName.toLowerCase().contains(lowerCaseSearchQuery)) {
            return true;
        } else if (packageName.equals(appName)) {
            return false;
        } else if (appName.toLowerCase().contains(lowerCaseSearchQuery)) {
            return true;
        } else {
            return false;
        }
    }

    public void cancelSearch() {
        this.searchEnabled = false;
        this.searchFilteredAppInfoList = null;
        notifyDataSetChanged();
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.perspective_state_app_item, null);
        }
        List<AppInfo> appInfoList;
        if (searchEnabled) {
            appInfoList = searchFilteredAppInfoList;
        } else {
            appInfoList = this.appInfoList;
        }

        String packageName = appInfoList.get(position).getPackageName();
        String appName = appNameProvider.getAppName(packageName);

        convertView.setTag(packageName);
        TextView textView = (TextView) convertView.findViewById(R.id.packagename);;
        textView.setText(appName);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.appicon);
        imageView.setImageDrawable(appIconProvider.getAppIcon(packageName));

        if (appStateProvider.isPackageEnabled(packageName)) {
            convertView.setBackgroundColor(Color.WHITE);
        } else {
            convertView.setBackgroundColor(Color.GRAY);
        }
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String packageName = (String) v.getTag();
                appLauncher.launch(context, packageName);
            }
        });

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
        checkBox.setTag(packageName);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(appSelectedListener.isPackageNameSelected(packageName));
        checkBox.setOnCheckedChangeListener(appSelectedListener);

        return convertView;
    }

    @Override
    // observer for app selection
    public void update(Observable observable, Object data) {
        this.appInfoList = packageListProvider.getOrderedPackages();
        notifyDataSetChanged();
    }

    public AppAssetUpdateListener getAppAssetUpdateListener() {
        return appAssetUpdateListener;
    }

    public PackageStateUpdateListener getPackageStateUpdateListener() {
        return packageStateUpdateListener;
    }
}
