package com.mysticwind.disabledappmanager.ui.common;

import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.Collection;
import java.util.Observer;

public class PackageStateUpdateAsyncTask extends AsyncTask<Void, Void, Void> {
    private final PackageStateController packageStateController;
    private final Collection<String> packages;
    private final boolean state;
    private Dialog progressDialog;
    private Toast endingToast;
    private Observer observer;
    private Object message;

    public PackageStateUpdateAsyncTask(PackageStateController packageStateController,
                                       Collection<String> packages, boolean state) {
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

    public PackageStateUpdateAsyncTask withNotification(Observer observer, Object message) {
        this.observer = observer;
        this.message = message;
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
        if (observer != null) {
            observer.update(null, message);
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
