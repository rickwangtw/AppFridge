package com.mysticwind.disabledappmanager.ui.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

public class DialogHelper {
    public static Dialog newProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle(R.string.progress_dialog_title_update_app_status);
        progressDialog.setIndeterminate(true);
        return progressDialog;
    }

    public static Dialog newNewAppGroupDialog(final Context context,
                  final PackageListProvider packageListProvider,
                  final AppIconProvider appIconProvider, final AppNameProvider appNameProvider,
                  final AppGroupManager appGroupManager, final Observer observer) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.new_app_group_dialog_title);

        final EditText appGroupNameEditText = new EditText(context);
        appGroupNameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(appGroupNameEditText);

        builder.setPositiveButton(R.string.new_app_group_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String appGroupName = appGroupNameEditText.getText().toString();
                newPackageListForAddingToGroupDialog(context, appGroupName, packageListProvider,
                        appIconProvider, appNameProvider, appGroupManager, new Observer() {
                            @Override
                            public void update(Observable observable, Object data) {
                                observer.update(null, Action.APP_GROUP_UPDATED);
                            }
                        }).show();
            }
        });
        builder.setNegativeButton(R.string.new_app_group_dialog_negative_button, null);

        return builder.create();
    }

    public static Dialog newConfirmDeleteAppGroupDialog(Context context, final String appGroupName,
            final AppGroupManager appGroupManager, final Observer observer) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        String titlePrefix = context.getResources().getString(
                R.string.confirm_delete_app_group_dialog_title_prefix);
        alertDialogBuilder.setTitle(titlePrefix + " " + appGroupName);
        alertDialogBuilder.setPositiveButton(
                R.string.confirm_delete_app_group_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appGroupManager.deleteAppGroup(appGroupName);
                observer.update(null, Action.APP_GROUP_UPDATED);
            }
        });
        alertDialogBuilder.setNegativeButton(
                R.string.confirm_delete_app_group_dialog_negative_button, null);
        return alertDialogBuilder.create();
    }

    public static Dialog newConfirmDeletePackageFromAppGroupDialog(final Context context,
           final String packageName, final String appGroupName,
           final AppGroupManager appGroupManager, final Observer observer) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        String titlePrefix = context.getResources().getString(
                R.string.confirm_delete_package_from_app_group_dialog_title_prefix);
        alertDialogBuilder.setTitle(titlePrefix + " " + appGroupName);

        String messagePrefix = context.getResources().getString(
                R.string.confirm_delete_package_from_app_group_dialog_msg_prefix);
        alertDialogBuilder.setMessage(messagePrefix + " " + packageName);

        alertDialogBuilder.setPositiveButton(
                R.string.confirm_delete_package_from_app_group_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appGroupManager.deletePackageFromAppGroup(packageName, appGroupName);
                observer.update(null, Action.PACKAGE_REMOVED_FROM_APP_GROUP);
            }
        });
        alertDialogBuilder.setNegativeButton(
                R.string.confirm_delete_app_group_dialog_negative_button, null);
        return alertDialogBuilder.create();
    }

    public static Dialog newPackageListForAddingToGroupDialog(final Context context,
              final String appGroupName, PackageListProvider packageListProvider,
              AppIconProvider appIconProvider, AppNameProvider appNameProvider,
              final AppGroupManager appGroupManager, final Observer observer) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        List<AppInfo> allPackages = packageListProvider.getOrderedPackages();
        Set<String> packagesInAppGroup = appGroupManager.getPackagesOfAppGroup(appGroupName);
        final List<AppInfo> packagesNotInAppGroup = new ArrayList<>(
                allPackages.size() - packagesInAppGroup.size());
        for (AppInfo packages : allPackages) {
            if (packagesInAppGroup.contains(packages.getPackageName())) {
                continue;
            }
            packagesNotInAppGroup.add(packages);
        }
        final Set<String> selectedPackages = new HashSet<>();
        alertDialogBuilder.setAdapter(
                new AppListAdapter(context, appIconProvider, appNameProvider, packagesNotInAppGroup,
                        new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        String checkedPackage = (String) buttonView.getTag();
                        if (isChecked) {
                            selectedPackages.add(checkedPackage);
                        } else {
                            selectedPackages.remove(checkedPackage);
                        }
                    }
                }),
                null);

        String titlePrefix = context.getResources().getString(
                R.string.package_list_for_adding_to_group_dialog_title_prefix);
        alertDialogBuilder.setTitle(titlePrefix + " " + appGroupName);
        alertDialogBuilder.setPositiveButton(
                R.string.package_list_for_adding_to_group_dialog_positive_button,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (selectedPackages.isEmpty()) {
                    return;
                }
                Toast.makeText(context,
                        context.getResources().getString(
                                R.string.toast_add_packages_to_app_group_msg_prefix)
                                + " " + selectedPackages + " -> " + appGroupName,
                        Toast.LENGTH_LONG).show();
                appGroupManager.addPackagesToAppGroup(selectedPackages, appGroupName);
                observer.update(null, Action.PACKAGE_ADDED_TO_APP_GROUP);
            }
        });
        alertDialogBuilder.setNegativeButton(
                R.string.package_list_for_adding_to_group_dialog_negative_button, null);
        return alertDialogBuilder.create();
    }

    private static class AppListAdapter extends ArrayAdapter<AppInfo> {
        private final AppIconProvider appIconProvider;
        private final AppNameProvider appNameProvider;
        private CompoundButton.OnCheckedChangeListener onCheckedChangeListener;

        public AppListAdapter(Context context, AppIconProvider appIconProvider,
                              AppNameProvider appNameProvider, Collection<AppInfo> packageList,
                              CompoundButton.OnCheckedChangeListener onCheckedChangeListener) {
            super(context, 0, new ArrayList<AppInfo>(packageList));
            this.appIconProvider = appIconProvider;
            this.appNameProvider = appNameProvider;
            this.onCheckedChangeListener = onCheckedChangeListener;
        }

        Set<Integer> selectedPositions = new HashSet<>();

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            AppInfo packageInfo = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.applistitem, parent, false);
            }
            ImageView icon = (ImageView) convertView.findViewById(R.id.appicon);
            icon.setImageDrawable(appIconProvider.getAppIcon(packageInfo.getPackageName()));
            TextView textView = (TextView) convertView.findViewById(R.id.packagename);
            textView.setText(appNameProvider.getAppName(packageInfo.getPackageName()));
            CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
            checkBox.setTag(packageInfo.getPackageName());
            /* prevent unexpected behavior when setting the status  */
            checkBox.setOnCheckedChangeListener(null);
            if (selectedPositions.contains(position)) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        selectedPositions.add(position);
                    } else {
                        selectedPositions.remove(position);
                    }
                    onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
                }
            });

            return convertView;
        }
    }
}
