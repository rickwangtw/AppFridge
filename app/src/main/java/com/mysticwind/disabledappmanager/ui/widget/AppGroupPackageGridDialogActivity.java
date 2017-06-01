package com.mysticwind.disabledappmanager.ui.widget;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.AssetType;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EActivity
public class AppGroupPackageGridDialogActivity extends AppCompatActivity implements AppAssetUpdateListener {
    private static final int DEFAULT_COLUMN_COUNT = 4;

    private String appGroupName;
    private AppGroupManager appGroupManager;
    private PackageAssetService packageAssetService;
    private AppLauncher appLauncher;
    private AppAssetUpdateEventManager appAssetUpdateEventManager;
    private int appGridLayoutWidth;
    private Map<String, ViewHolder> packageNameToViewHolder = new ConcurrentHashMap<>();

    @ViewById(R.id.app_grid)
    GridLayout appGridLayout;

    @Value
    private static class PackageItem {
        private String packageName;
        private String appName;
        private Drawable icon;
    }

    @Value
    private static class ViewHolder {
        ImageView iconImageView;
        TextView appNameTextView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appGroupManager = ApplicationHelper.from(this).appGroupManager();
        packageAssetService = ApplicationHelper.from(this).packageAssetService();
        appLauncher = ApplicationHelper.from(this).appLauncher();
        appAssetUpdateEventManager = ApplicationHelper.from(this).appAssetUpdateEventManager();

        appAssetUpdateEventManager.registerListener(this);

        appGroupName = getIntent().getStringExtra(WidgetConstants.APP_GROUP_NAME_EXTRA_KEY);
        setTitle(appGroupName);
        setContentView(R.layout.app_group_launching_intent_dialog);
    }

    @AfterViews
    void initAppGrid() {
        appGridLayout.setColumnCount(getColumnCount());
        appGridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (appGridLayoutWidth != appGridLayout.getWidth()) {
                    appGridLayoutWidth = appGridLayout.getWidth();
                    log.debug("onGlobalLayout: " + appGridLayout.getWidth());
                    loadAppGrid();
                }
            }
        });
    }

    private int getColumnCount() {
        return DEFAULT_COLUMN_COUNT;
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadAppGrid();
    }

    private void loadAppGrid() {
        int width = appGridLayoutWidth;
        if (width == 0) {
            return;
        }
        int itemWidth = width / getColumnCount();
        log.debug("width: " + width + " item: " + itemWidth);

        appGridLayout.removeAllViewsInLayout();

        List<PackageItem> packageItemList = getSortedPackagesForAppGroup(appGroupName);

        List<List<PackageItem>> partitionedPackageItemList = Lists.partition(packageItemList, getColumnCount());

        for (int rowIndex = 0; rowIndex < partitionedPackageItemList.size() ; ++rowIndex) {
            List<PackageItem> packageItemListRow  = partitionedPackageItemList.get(rowIndex);
            for (int columnIndex = 0 ; columnIndex < packageItemListRow.size() ; ++columnIndex) {
                final PackageItem packageItem = packageItemListRow.get(columnIndex);

                View appItemView = LayoutInflater.from(this).inflate(
                        R.layout.app_group_launching_intent_dialog_grid_item, null);
                ImageView icon = (ImageView) appItemView.findViewById(R.id.appicon);
                TextView packageName = (TextView) appItemView.findViewById(R.id.packagename);

                updateViewHolderOfPackage(icon, packageName, packageItem.getPackageName());

                GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams(
                        GridLayout.spec(rowIndex), GridLayout.spec(columnIndex));
                layoutParams.width = itemWidth;
                appItemView.setLayoutParams(layoutParams);

                icon.setImageDrawable(packageItem.getIcon());
                packageName.setText(packageItem.getAppName());

                appItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        appLauncher.launch(
                                AppGroupPackageGridDialogActivity.this, packageItem.getPackageName());
                    }
                });
                appGridLayout.addView(appItemView, layoutParams);
            }
        }
    }

    private void updateViewHolderOfPackage(
            ImageView iconImageView, TextView appNameTextView, String packageName) {
        packageNameToViewHolder.put(packageName, new ViewHolder(iconImageView, appNameTextView));
    }

    private List<PackageItem> getSortedPackagesForAppGroup(String appGroupName) {
        Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroupName);

        List<PackageItem> packageItemList = new LinkedList<>();

        for (String packageName : packages) {
            final PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
            packageItemList.add(new PackageItem(
                    packageName,
                    packageAssets.getAppName(),
                    packageAssets.getIconDrawable()));
        }
        Collections.sort(packageItemList, new Comparator<PackageItem>() {
            @Override
            public int compare(PackageItem lhs, PackageItem rhs) {
                String lhsAppName = lhs.getAppName();
                if (lhsAppName == null) {
                    log.warn("No app name for package " + lhs.getPackageName());
                    lhsAppName = "";
                }
                String rhsAppName = rhs.getAppName();
                if (rhsAppName == null) {
                    log.warn("No app name for package " + rhs.getPackageName());
                    rhsAppName = "";
                }
                return lhsAppName.compareTo(rhsAppName);
            }
        });
        return packageItemList;
    }

    @Override
    public void update(AppAssetUpdate event) {
        ViewHolder viewHolder = packageNameToViewHolder.get(event.getPackageName());
        if (viewHolder == null) {
            return;
        }
        if (event.getUpdatedAssetTypes().contains(AssetType.ICON)) {
            updateIcon(event.getPackageName(), viewHolder.getIconImageView());
        }
        if (event.getUpdatedAssetTypes().contains(AssetType.APP_NAME)) {
            updateAppName(event.getPackageName(), viewHolder.getAppNameTextView());
        }
    }

    private void updateIcon(String packageName, ImageView iconImageView) {
        final PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
        iconImageView.setImageDrawable(packageAssets.getIconDrawable());
    }

    private void updateAppName(String packageName, TextView appNameTextView) {
        final PackageAssets packageAssets = packageAssetService.getPackageAssets(packageName);
        appNameTextView.setText(packageAssets.getAppName());
    }
}
