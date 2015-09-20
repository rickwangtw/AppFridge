package com.mysticwind.disabledappmanager;

import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

public class AppSelectedListener extends Observable
        implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG ="AppSelectedListener";

    private final Dialog progressDialog;
    private final Dialog appGroupDialog;
    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final Set<String> selectedPackageNames = new HashSet<>();

    public AppSelectedListener(Dialog progressDialog, Dialog appGroupDialog, PackageStateController packageStateController, AppStateProvider appStateProvider) {
        this.progressDialog = progressDialog;
        this.appGroupDialog = appGroupDialog;
        this.packageStateController = packageStateController;
        this.appStateProvider = appStateProvider;
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
}
