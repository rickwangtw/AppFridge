package com.mysticwind.disabledappmanager.launcher;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.ui.common.Action;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.common.SwipeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

public class AppGroupListAdapter extends BaseExpandableListAdapter
        implements AdapterView.OnItemLongClickListener, ExpandableListView.OnChildClickListener,
        ExpandableListView.OnGroupClickListener, Observer {
    private static final String TAG = "AppGroupListAdapter";

    private final Context context;
    private final AppGroupManager appGroupManager;
    private final LayoutInflater layoutInflator;
    private final SwipeDetector swipeDetector;
    private final AppIconProvider appIconProvider;
    private final AppNameProvider appNameProvider;
    private final AppStateProvider appStateProvider;
    private final PackageStateController packageStateController;
    private final AppLauncher appLauncher;
    private final List<String> allAppGroups;
    private final Map<String, List<String>> appGroupToPackageListMap = new HashMap<>();
    private final Dialog groupActionDialog;
    private String selectedAppGroupName;

    public AppGroupListAdapter(Context context, AppGroupManager appGroupManager,
                               AppIconProvider appIconProvider, AppNameProvider appNameProvider,
                               AppStateProvider appStateProvider,
                               PackageStateController packageStateController,
                               AppLauncher appLauncher, LayoutInflater layoutInflator,
                               SwipeDetector swipeDetector) {
        this.context = context;
        this.appGroupManager = appGroupManager;
        this.appIconProvider = appIconProvider;
        this.appNameProvider = appNameProvider;
        this.appStateProvider = appStateProvider;
        this.packageStateController = packageStateController;
        this.appLauncher = appLauncher;
        this.layoutInflator = layoutInflator;
        this.swipeDetector = swipeDetector;
        this.allAppGroups = getSortedAllAppGroups();
        this.groupActionDialog = buildGroupActionDialog();
    }

    private List<String> getSortedAllAppGroups() {
        List<String> allAppGroups = new ArrayList<String>(appGroupManager.getAllAppGroups());
        Collections.sort(allAppGroups);
        return allAppGroups;
    }

    private String getAppGroup(int groupPosition) {
        String appGroup = allAppGroups.get(groupPosition);
        return appGroup;
    }

    private List<String> getPackageListOfGroupPosition(int groupPosition) {
        String appGroup = getAppGroup(groupPosition);
        return getPackageListOfAppGroupName(appGroup);
    }

    private List<String> getPackageListOfAppGroupName(String appGroupName) {
        List<String> packageNameList = appGroupToPackageListMap.get(appGroupName);
        if (packageNameList == null) {
            packageNameList = new ArrayList<>(appGroupManager.getPackagesOfAppGroup(appGroupName));
            Collections.sort(packageNameList);
            appGroupToPackageListMap.put(appGroupName, packageNameList);
        }
        return packageNameList;
    }

    private String getPackageNameOfGroupPositionChildPosition(int groupPosition, int childPosition) {
        return getPackageListOfGroupPosition(groupPosition).get(childPosition);
    }

    @Override
    public int getGroupCount() {
        return allAppGroups.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<String> packageNameList = getPackageListOfGroupPosition(groupPosition);
        return packageNameList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return getAppGroup(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(R.layout.launcher_group_item, null);
        }
        String appGroup = getAppGroup(groupPosition);

        convertView.setTag(appGroup);
        TextView appGroupNameTextView = (TextView) convertView.findViewById(R.id.app_group_name);
        appGroupNameTextView.setText(appGroup);
        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflator.inflate(R.layout.launcher_app_item, null);
        }
        String packageName
                = getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);

        ImageView iconView = (ImageView) convertView.findViewById(R.id.appicon);
        TextView packageNameTextView = (TextView) convertView.findViewById(R.id.packagename);

        iconView.setImageDrawable(appIconProvider.getAppIcon(packageName));
        packageNameTextView.setText(appNameProvider.getAppName(packageName));
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        String appGroupName = (String) view.getTag();
        Log.d(TAG, "Long clicked group name: " + appGroupName);
        this.selectedAppGroupName = appGroupName;
        groupActionDialog.setTitle("Actions for " + appGroupName);
        groupActionDialog.show();

        return true;
    }

    private Dialog buildGroupActionDialog() {
        AlertDialog.Builder groupActionDialogBuilder = new AlertDialog.Builder(context)
                .setPositiveButton("Enable Apps", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> packages = getPackageListOfAppGroupName(selectedAppGroupName);
                        Log.d(TAG, "Enable apps: " + packages);
                        packageStateController.enablePackages(packages);
                        Toast.makeText(context, "Enabled packages: " + packages,
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .setNeutralButton("Disable Apps", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> packages = getPackageListOfAppGroupName(selectedAppGroupName);
                        Log.d(TAG, "Disable apps: " + packages);
                        packageStateController.disablePackages(packages);
                        Toast.makeText(context, "Disabled packages: " + packages,
                                Toast.LENGTH_SHORT).show();
                    }
                });
        return groupActionDialogBuilder.create();
    }

    @Override
    public boolean onChildClick(
            ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        final String appGroupName = getAppGroup(groupPosition);
        final String packageName =
                getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);
        if (swipeDetector.swipeDetected()) {
            if (SwipeDetector.Action.RIGHT_TO_LEFT == swipeDetector.getAction()) {
                final ImageButton imageButton = (ImageButton) v.findViewById(R.id.trashButton);
                imageButton.setVisibility(View.VISIBLE);
                imageButton.setFocusable(false);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogHelper.newConfirmDeletePackageFromAppGroupDialog(context, packageName,
                                appGroupName, appGroupManager, AppGroupListAdapter.this).show();
                        imageButton.setVisibility(View.GONE);
                    }
                });
                return true;
            } else if (SwipeDetector.Action.LEFT_TO_RIGHT == swipeDetector.getAction()) {
                ImageButton imageButton = (ImageButton) v.findViewById(R.id.trashButton);
                imageButton.setVisibility(View.GONE);
                imageButton.setFocusable(false);
                return true;
            }
        }
        boolean isEnabled = appStateProvider.isPackageEnabled(packageName);
        if (!isEnabled) {
            Toast.makeText(
                    context, "Enabling package: " + packageName, Toast.LENGTH_SHORT).show();
            packageStateController.enablePackages(Arrays.asList(packageName));
        }
        Intent intent = appLauncher.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            Toast.makeText(context,
                    "No launching intent for package: " + packageName, Toast.LENGTH_SHORT).show();
        } else {
            context.startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        if (swipeDetector.swipeDetected()) {
            if (SwipeDetector.Action.RIGHT_TO_LEFT == swipeDetector.getAction()) {
                final ImageButton imageButton = (ImageButton) v.findViewById(R.id.trashButton);
                imageButton.setVisibility(View.VISIBLE);
                imageButton.setFocusable(false);
                final String appGroupName = getAppGroup(groupPosition);
                imageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogHelper.newConfirmDeleteAppGroupDialog(context, appGroupName,
                                appGroupManager, AppGroupListAdapter.this).show();
                        imageButton.setVisibility(View.GONE);
                    }
                });
                return true;
            } else if (SwipeDetector.Action.LEFT_TO_RIGHT == swipeDetector.getAction()) {
                ImageButton imageButton = (ImageButton) v.findViewById(R.id.trashButton);
                imageButton.setVisibility(View.GONE);
                imageButton.setFocusable(false);
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(Observable observable, Object data) {
        Action action = (Action) data;
        switch (action) {
            case APP_GROUP_UPDATED:
                selectedAppGroupName = null;
                allAppGroups.clear();
                allAppGroups.addAll(getSortedAllAppGroups());
                appGroupToPackageListMap.clear();
                notifyDataSetChanged();
                break;
            case PACKAGE_REMOVED_FROM_APP_GROUP:
                appGroupToPackageListMap.clear();
                notifyDataSetChanged();
                break;
        }
    }
}
