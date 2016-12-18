package com.mysticwind.disabledappmanager.ui.common;

import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PackageStateUpdateAsyncTask extends AsyncTask<Void, Void, Collection<String>> {

    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final Collection<String> packages;
    private final boolean state;

    private Dialog progressDialog;
    private Toast endingToast;

    public PackageStateUpdateAsyncTask(PackageStateController packageStateController,
                                       AppStateProvider appStateProvider,
                                       Collection<String> packages, boolean state) {
        this.packageStateController = packageStateController;
        this.appStateProvider = appStateProvider;
        this.packages = packages;
        this.state = state;
    }

    public PackageStateUpdateAsyncTask withProgressDialog(Dialog progressDialog) {
        this.progressDialog = progressDialog;
        return this;
    }

    public PackageStateUpdateAsyncTask withEndingToast(Toast endingToast) {
        this.endingToast = endingToast;
        return this;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Collection<String> actionedPackages) {
        super.onPostExecute(actionedPackages);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (endingToast != null) {
            endingToast.show();
        }
        log.debug("Actioned (" + state + ") packages: " + actionedPackages);
    }

    @Override
    protected Collection<String> doInBackground(Void... params) {
        Set<String> needActionPackages = new HashSet<>();
        for (String packageName : packages) {
            if (state ^ appStateProvider.isPackageEnabled(packageName)) {
                needActionPackages.add(packageName);
            }
        }
        if (state) {
            packageStateController.enablePackages(needActionPackages);
        } else {
            packageStateController.disablePackages(needActionPackages);
        }
        return needActionPackages;
    }
}
