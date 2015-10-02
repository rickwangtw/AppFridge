package com.mysticwind.disabledappmanager.ui.common;

import android.app.Dialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.Collection;
import java.util.HashSet;
import java.util.Observer;
import java.util.Set;

import de.greenrobot.event.EventBus;

public class PackageStateUpdateAsyncTask extends AsyncTask<Void, Void, Collection<String>> {
    private static final String TAG = "PackageStateUpdateTask";
    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final Collection<String> packages;
    private final boolean state;
    private Dialog progressDialog;
    private Toast endingToast;
    private Observer observer;
    private Object message;
    private Object eventToPost;

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

    public PackageStateUpdateAsyncTask withNotification(Observer observer, Object message) {
        this.observer = observer;
        this.message = message;
        return this;
    }

    public PackageStateUpdateAsyncTask withCompletedEvent(Object event) {
        this.eventToPost = event;
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
        if (observer != null) {
            observer.update(null, message);
        }
        if (eventToPost != null) {
            EventBus.getDefault().post(eventToPost);
        }
        if (endingToast != null) {
            endingToast.show();
        }
        Log.d(TAG, "Actioned (" + state + ") packages: " + actionedPackages);
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
