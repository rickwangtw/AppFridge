package com.mysticwind.disabledappmanager.ui.activity.perspective.state;

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

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.ui.common.Action;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import de.greenrobot.event.EventBus;

public class AppListAdapter extends BaseAdapter implements Observer {
    private static final String TAG = "AppListAdapter";

    private final PackageListProvider packageListProvider;
    private final AppStateProvider appStateProvider;
    private final AppNameProvider appNameProvider;
    private final AppIconProvider appIconProvider;
    private final LayoutInflater layoutInflater;
    private final AppSelectedListener appSelectedListener;
    private List<AppInfo> appInfoList;

    public AppListAdapter(PackageListProvider packageListProvider, AppStateProvider appStateProvider,
                          AppIconProvider appIconProvider,
                          AppNameProvider appNameProvider,
                          LayoutInflater layoutInflater,
                          AppSelectedListener appSelectedListener) {
        this.packageListProvider = packageListProvider;
        this.appStateProvider = appStateProvider;
        this.appNameProvider = appNameProvider;
        this.appIconProvider = appIconProvider;
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

    private Map<Integer, CachedAppInfo> positionToViewMap = new HashMap<>();

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = layoutInflater.inflate(R.layout.perspective_state_app_item, null);
        }
        CachedAppInfo cachedAppInfoForPosition = positionToViewMap.get(position);
        if (cachedAppInfoForPosition == null) {
            cachedAppInfoForPosition = newViewHolder(view, position);
            positionToViewMap.put(position, cachedAppInfoForPosition);
        }

        TextView textView = (TextView) view.findViewById(R.id.packagename);;
        textView.setText(appNameProvider.getAppName(cachedAppInfoForPosition.packageName));

        ImageView imageView = (ImageView) view.findViewById(R.id.appicon);
        imageView.setImageDrawable(appIconProvider.getAppIcon(cachedAppInfoForPosition.packageName));

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
        AppInfo appInfo = appInfoList.get(position);

        final CachedAppInfo cachedAppInfo = new CachedAppInfo();
        cachedAppInfo.packageName = appInfo.getPackageName();
        cachedAppInfo.appInfo = appInfo;
        CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
        checkBox.setOnCheckedChangeListener(appSelectedListener);
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
        if (event == Action.PACKAGE_ASSET_UPDATED) {
            notifyDataSetChanged();
        }
    }
}
