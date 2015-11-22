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

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.ui.common.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import de.greenrobot.event.EventBus;

public class AppListAdapter extends BaseAdapter implements Observer, AppAssetUpdateListener {
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
    private Map<Integer, CachedAppInfo> positionToViewMap = new HashMap<>();

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

        EventBus.getDefault().register(this);
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
        public AppInfo appInfo;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.perspective_state_app_item, null);
        }
        CachedAppInfo cachedAppInfoForPosition = positionToViewMap.get(position);
        if (cachedAppInfoForPosition == null) {
            cachedAppInfoForPosition = newViewHolder(position);
            positionToViewMap.put(position, cachedAppInfoForPosition);
        }

        convertView.setTag(cachedAppInfoForPosition.packageName);
        TextView textView = (TextView) convertView.findViewById(R.id.packagename);;
        textView.setText(appNameProvider.getAppName(cachedAppInfoForPosition.packageName));

        ImageView imageView = (ImageView) convertView.findViewById(R.id.appicon);
        imageView.setImageDrawable(appIconProvider.getAppIcon(cachedAppInfoForPosition.packageName));

        if (appStateProvider.isPackageEnabled(cachedAppInfoForPosition.packageName)) {
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
        checkBox.setTag(cachedAppInfoForPosition.packageName);
        checkBox.setOnCheckedChangeListener(null);
        checkBox.setChecked(
                appSelectedListener.isPackageNameSelected(cachedAppInfoForPosition.packageName));
        checkBox.setOnCheckedChangeListener(appSelectedListener);

        return convertView;
    }

    private CachedAppInfo newViewHolder(int position) {
        AppInfo appInfo = appInfoList.get(position);

        final CachedAppInfo cachedAppInfo = new CachedAppInfo();
        cachedAppInfo.packageName = appInfo.getPackageName();
        cachedAppInfo.appInfo = appInfo;
        return cachedAppInfo;
    }

    @Override
    public void update(Observable observable, Object data) {
        this.appInfoList = packageListProvider.getOrderedPackages();
        this.positionToViewMap.clear();
        notifyDataSetChanged();
    }

    // This method will be called from EventBus when Action is called
    public void onEventMainThread(Action event){
        switch (event) {
            case PACKAGE_STATE_UPDATED:
                this.appInfoList = packageListProvider.getOrderedPackages();
                positionToViewMap.clear();
                notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void update(AppAssetUpdate event) {
        notifyDataSetChanged();
    }
}
