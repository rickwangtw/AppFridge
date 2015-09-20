package com.mysticwind.disabledappmanager;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class AppSelectedListener extends Observable
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, DialogInterface.OnClickListener {
    private static final String TAG ="AppSelectedListener";

    private final Dialog progressDialog;
    private final Dialog appGroupDialog;
    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final AppGroupManager appGroupManager;
    private final Set<String> selectedPackageNames = new HashSet<>();

    public AppSelectedListener(Context context, LayoutInflater layoutInflater,
                               PackageStateController packageStateController,
                               AppStateProvider appStateProvider, AppGroupManager appGroupManager) {
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Updating application status");
        progressDialog.setIndeterminate(true);
        this.progressDialog = progressDialog;

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
        dialogBuilder.setView(layoutInflater.inflate(R.layout.app_group_dialog, null));
        dialogBuilder.setTitle("Enter Group Name");
        dialogBuilder.setPositiveButton("Create", this);
        dialogBuilder.setNegativeButton("Cancel", null);
        this.appGroupDialog = dialogBuilder.create();

        this.packageStateController = packageStateController;
        this.appStateProvider = appStateProvider;
        this.appGroupManager = appGroupManager;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String packageName = (String) buttonView.getTag();
        if (isChecked) {
            selectedPackageNames.add(packageName);
        } else {
            selectedPackageNames.remove(packageName);
        }
        Log.d(TAG, "Selected packages: " + selectedPackageNames);
    }

    public boolean isPackageNameSelected(String packageName) {
        return selectedPackageNames.contains(packageName);
    }

    @Override
    public void onClick(View v) {
        if (selectedPackageNames.isEmpty()) {
            Log.i(TAG, "No package selected ...");
            return;
        }

        final int id = v.getId();
        if (id == R.id.add_to_group_button) {
            appGroupDialog.show();
            return;
        }

        final AsyncTask<String, Void, Void> packageStateUpdateTask = new AsyncTask<String, Void, Void>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                progressDialog.show();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                setChanged();
                notifyObservers();

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }

            @Override
            protected Void doInBackground(String... params) {
                switch (id) {
                    case R.id.enable_app_button:
                        packageStateController.enablePackages(selectedPackageNames);
                        break;
                    case R.id.disable_app_button:
                        packageStateController.disablePackages(selectedPackageNames);
                        break;
                    case R.id.toggle_app_state_button:
                        togglePackages(selectedPackageNames);
                        break;
                    default:
                        Log.w(TAG, "Unsupported click action for view: " + id);
                }
                selectedPackageNames.clear();
                return null;
            }
        };
        packageStateUpdateTask.execute();
    }

    private void togglePackages(Set<String> packageNames) {
        Set<String> packagesToEnable = new HashSet<>();
        Set<String> packagesToDisable = new HashSet<>();

        for (String packageName : selectedPackageNames) {
            if (appStateProvider.isPackageEnabled(packageName)) {
                packagesToDisable.add(packageName);
            } else {
                packagesToEnable.add(packageName);
            }
        }
        if (!packagesToDisable.isEmpty()) {
            packageStateController.disablePackages(packagesToDisable);
        }
        if (!packagesToEnable.isEmpty()) {
            packageStateController.enablePackages(packagesToEnable);
        }
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        TextView appGroupNameTextView = (TextView) appGroupDialog.findViewById(R.id.app_group_name);
        String appGroupName = appGroupNameTextView.getText().toString();
        Log.i(TAG, "Adding " + selectedPackageNames + " to " + appGroupName);
        appGroupManager.addPackagesToAppGroup(
                new HashSet<String>(selectedPackageNames), appGroupName);

        selectedPackageNames.clear();
        setChanged();
        notifyObservers();
    }
}
