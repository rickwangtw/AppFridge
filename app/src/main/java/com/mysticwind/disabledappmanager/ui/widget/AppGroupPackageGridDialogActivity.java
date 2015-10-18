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
import com.mysticwind.disabledappmanager.domain.PackageAssetService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import lombok.Value;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@EActivity
public class AppGroupPackageGridDialogActivity extends AppCompatActivity {
    private static final int DEFAULT_COLUMN_COUNT = 4;

    private String appGroupName;
    private AppGroupManager appGroupManager;
    private PackageAssetService packageAssetService;
    private AppLauncher appLauncher;
    private int appGridLayoutWidth;

    @ViewById(R.id.app_grid)
    GridLayout appGridLayout;

    @Value
    private static class PackageItem {
        private String packageName;
        private String appName;
        private Drawable icon;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appGroupManager = ApplicationHelper.from(this).appGroupManager();
        packageAssetService = ApplicationHelper.from(this).packageAssetService();
        appLauncher = ApplicationHelper.from(this).appLauncher();

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

    private List<PackageItem> getSortedPackagesForAppGroup(String appGroupName) {
        Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroupName);

        List<PackageItem> packageItemList = new LinkedList<>();

        for (String packageName : packages) {
            packageItemList.add(new PackageItem(
                    packageName,
                    packageAssetService.getAppName(packageName),
                    packageAssetService.getAppIcon(packageName)));
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
}
