package com.mysticwind.disabledappmanager.ui.activity.perspective.group;

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
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.ui.common.Action;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;
import com.mysticwind.disabledappmanager.ui.common.PackageStateUpdateAsyncTask;
import com.mysticwind.disabledappmanager.ui.common.SwipeDetector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

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
    private final Dialog progressDialog;
    private final String allAppGroupName;
    private String selectedAppGroupName;
    private PackageListProvider packageListProvider;

    public AppGroupListAdapter(Context context, AppGroupManager appGroupManager,
                               AppIconProvider appIconProvider, AppNameProvider appNameProvider,
                               AppStateProvider appStateProvider,
                               PackageListProvider packageListProvider,
                               PackageStateController packageStateController,
                               AppLauncher appLauncher, LayoutInflater layoutInflator,
                               SwipeDetector swipeDetector) {
        this.context = context;
        this.appGroupManager = appGroupManager;
        this.appIconProvider = appIconProvider;
        this.appNameProvider = appNameProvider;
        this.appStateProvider = appStateProvider;
        this.packageListProvider = packageListProvider;
        this.packageStateController = packageStateController;
        this.appLauncher = appLauncher;
        this.layoutInflator = layoutInflator;
        this.swipeDetector = swipeDetector;
        this.allAppGroupName = context.getResources().getString(R.string.generated_app_group_name_all);
        this.allAppGroups = getSortedAllAppGroups();
        this.groupActionDialog = buildGroupActionDialog();
        this.progressDialog = DialogHelper.newProgressDialog(context);
    }

    private List<String> getSortedAllAppGroups() {
        List<String> allAppGroups = new ArrayList<String>(appGroupManager.getAllAppGroups());
        Collections.sort(allAppGroups);
        allAppGroups.add(allAppGroupName);
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
            Set<String> appGroupSet;
            if (allAppGroupName.equals(appGroupName)) {
                appGroupSet = new HashSet<>();
                for (AppInfo packageInfo : packageListProvider.getPackages()) {
                    appGroupSet.add(packageInfo.getPackageName());
                }
            } else {
                appGroupSet = appGroupManager.getPackagesOfAppGroup(appGroupName);
            }
            packageNameList = new ArrayList<>(appGroupSet);
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
        final String packageName
                = getPackageNameOfGroupPositionChildPosition(groupPosition, childPosition);

        ImageView iconView = (ImageView) convertView.findViewById(R.id.appicon);
        TextView packageNameTextView = (TextView) convertView.findViewById(R.id.packagename);
        Switch appStateSwitch = (Switch) convertView.findViewById(R.id.appStateSwitch);
        /* prevent unexpected behavior */
        appStateSwitch.setOnCheckedChangeListener(null);
        appStateSwitch.setChecked(appStateProvider.isPackageEnabled(packageName));
        appStateSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    String enablingToastMessagePrefix = context.getResources().getString(
                            R.string.toast_enabled_packages_msg_prefix);
                    Toast toast = Toast.makeText(context,
                            enablingToastMessagePrefix + " " + packageName, Toast.LENGTH_SHORT);
                    new PackageStateUpdateAsyncTask(
                            packageStateController,
                            appStateProvider,
                            Arrays.asList(packageName),
                            true
                    ).withEndingToast(toast).execute();
                } else {
                    String disablingToastMessagePrefix = context.getResources().getString(
                            R.string.toast_disabled_packages_msg_prefix);
                    Toast toast = Toast.makeText(context,
                            disablingToastMessagePrefix + " " + packageName, Toast.LENGTH_SHORT);
                    new PackageStateUpdateAsyncTask(
                            packageStateController,
                            appStateProvider,
                            Arrays.asList(packageName),
                            false
                    ).withEndingToast(toast).execute();
                }
            }
        });

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

        String titlePrefix = context.getResources().getString(
                R.string.group_action_dialog_title_prefix);
        groupActionDialog.setTitle(titlePrefix + " " + appGroupName);
        groupActionDialog.show();

        return true;
    }

    private Dialog buildGroupActionDialog() {
        AlertDialog.Builder groupActionDialogBuilder = new AlertDialog.Builder(context)
                .setPositiveButton(R.string.group_action_dialog_enable_packages_button,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> packages = getPackageListOfAppGroupName(selectedAppGroupName);
                        Log.d(TAG, "Enable apps: " + packages);
                        new PackageStateUpdateAsyncTask(packageStateController, appStateProvider, packages, true)
                                .withProgressDialog(progressDialog)
                                .withEndingToast(Toast.makeText(context,
                                        context.getResources().getString(
                                                R.string.toast_enabled_packages_msg_prefix) + " " + packages,
                                        Toast.LENGTH_SHORT))
                                .withNotification(AppGroupListAdapter.this, Action.PACKAGE_STATE_UPDATED)
                                .execute();
                    }
                })
                .setNeutralButton(R.string.group_action_dialog_disable_packages_button,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<String> packages = getPackageListOfAppGroupName(selectedAppGroupName);
                        Log.d(TAG, "Disable apps: " + packages);
                        new PackageStateUpdateAsyncTask(packageStateController, appStateProvider, packages, false)
                                .withProgressDialog(progressDialog)
                                .withEndingToast(Toast.makeText(context,
                                        context.getResources().getString(
                                                R.string.toast_disabled_packages_msg_prefix)+ " " + packages,
                                        Toast.LENGTH_SHORT))
                                .withNotification(AppGroupListAdapter.this, Action.PACKAGE_STATE_UPDATED)
                                .execute();
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
                    context, context.getResources().getString(
                            R.string.toast_enabled_packages_msg_prefix) + " " + packageName,
                    Toast.LENGTH_SHORT).show();
            packageStateController.enablePackages(Arrays.asList(packageName));
            update(null, Action.PACKAGE_STATE_UPDATED);
        }
        Intent intent = appLauncher.getLaunchIntentForPackage(packageName);
        if (intent == null) {
            Toast.makeText(context, context.getResources().getString(
                            R.string.toast_warn_no_launcher_intent_msg_prefix) + " " + packageName,
                    Toast.LENGTH_SHORT).show();
            /* disable back if the original status is disabled */
            if (!isEnabled) {
                Toast toast = Toast.makeText(context, context.getResources().getString(
                        R.string.toast_disabled_packages_msg_prefix) + " " + packageName,
                        Toast.LENGTH_SHORT);
                new PackageStateUpdateAsyncTask(packageStateController, appStateProvider, Arrays.asList(packageName), false)
                        .withNotification(AppGroupListAdapter.this, Action.PACKAGE_STATE_UPDATED)
                        .withEndingToast(toast)
                        .execute();
            }
        } else {
            context.startActivity(intent);
        }
        return true;
    }

    private boolean isGeneratedGroup(String appGroupName) {
        if (allAppGroupName.equals(appGroupName)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
        final String appGroupName = getAppGroup(groupPosition);
        if (!isGeneratedGroup(appGroupName) && swipeDetector.swipeDetected()) {
            final ImageButton addToGroupButton =
                    (ImageButton) v.findViewById(R.id.add_to_group_button);
            final ImageButton trashButton = (ImageButton) v.findViewById(R.id.trashButton);
            if (SwipeDetector.Action.RIGHT_TO_LEFT == swipeDetector.getAction()) {
                setGroupImageButtonAttributes(addToGroupButton, true);
                addToGroupButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogHelper.newPackageListForAddingToGroupDialog(context, appGroupName,
                                packageListProvider, appIconProvider, appNameProvider,
                                appGroupManager, AppGroupListAdapter.this).show();
                        setGroupImageButtonAttributes(addToGroupButton, false);
                        setGroupImageButtonAttributes(trashButton, false);
                    }
                });
                setGroupImageButtonAttributes(trashButton, true);
                trashButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DialogHelper.newConfirmDeleteAppGroupDialog(context, appGroupName,
                                appGroupManager, AppGroupListAdapter.this).show();
                        setGroupImageButtonAttributes(addToGroupButton, false);
                        setGroupImageButtonAttributes(trashButton, false);
                    }
                });
                return true;
            } else if (SwipeDetector.Action.LEFT_TO_RIGHT == swipeDetector.getAction()) {
                setGroupImageButtonAttributes(addToGroupButton, false);
                setGroupImageButtonAttributes(trashButton, false);
                return true;
            }
        }
        return false;
    }

    private void setGroupImageButtonAttributes(ImageButton imageButton, boolean visible) {
        setViewVisibility(imageButton, visible);
        setViewNotFocusable(imageButton);
    }

    private void setViewVisibility(View view, boolean visible) {
        view.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void setViewNotFocusable(View view) {
        view.setFocusable(false);
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
            case PACKAGE_ADDED_TO_APP_GROUP:
            case PACKAGE_REMOVED_FROM_APP_GROUP:
                appGroupToPackageListMap.clear();
                notifyDataSetChanged();
                break;
            case PACKAGE_STATE_UPDATED:
                notifyDataSetChanged();
                break;
        }
    }
}
