package com.mysticwind.disabledappmanager.ui.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;

import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.launcher.AppGroupListAdapter;

import java.util.Observer;

public class DialogHelper {
    public static Dialog newProgressDialog(Context context) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Updating application status");
        progressDialog.setIndeterminate(true);
        return progressDialog;
    }

    public static Dialog newConfirmDeleteAppGroupDialog(Context context, final String appGroupName,
            final AppGroupManager appGroupManager, final Observer observer) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Are you sure you want to delete " + appGroupName + "?");
        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appGroupManager.deleteAppGroup(appGroupName);
                observer.update(null, Action.APP_GROUP_UPDATED);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", null);
        return alertDialogBuilder.create();
    }

    public static Dialog newConfirmDeletePackageFromAppGroupDialog(final Context context,
           final String packageName, final String appGroupName,
           final AppGroupManager appGroupManager, final Observer observer) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle("Modify " + appGroupName);
        alertDialogBuilder.setMessage("Are you sure you want to remove " + packageName + "?");
        alertDialogBuilder.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                appGroupManager.deletePackageFromAppGroup(packageName, appGroupName);
                observer.update(null, Action.PACKAGE_REMOVED_FROM_APP_GROUP);
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", null);
        return alertDialogBuilder.create();
    }
}
