package com.mysticwind.disabledappmanager.ui.common;

import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.Collection;

public class PackageStateUpdateAsyncTask extends AsyncTask<Void, Void, Void> {
    private final PackageStateController packageStateController;
    private final Collection<String> packages;
    private final boolean state;
    private Dialog progressDialog;
    private Toast endingToast;

    public PackageStateUpdateAsyncTask(PackageStateController packageStateController,
                                       Collection<String> packages, boolean state) {
        this.progressDialog = progressDialog;
        this.packageStateController = packageStateController;
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
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (endingToast != null) {
            endingToast.show();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {
        if (state) {
            packageStateController.enablePackages(packages);
        } else {
            packageStateController.disablePackages(packages);
        }
        return null;
    }
}
