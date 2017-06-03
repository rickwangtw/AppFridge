package com.mysticwind.disabledappmanager.ui.common;

import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.common.collect.Sets;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageState;

import java.util.Collection;
import java.util.Set;

import java8.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class PackageStateUpdateAsyncTask extends AsyncTask<Void, Void, Collection<String>> {

    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final Set<String> packages;
    private final Action action;

    private Dialog progressDialog;
    private Toast endingToast;
    private ManualStateUpdateEventManager manualStateUpdateEventManager;

    public enum Action {
        ENABLE,
        DISABLE,
        TOGGLE,
    }

    public PackageStateUpdateAsyncTask(PackageStateController packageStateController,
                                       AppStateProvider appStateProvider,
                                       Collection<String> packages,
                                       Action action) {
        this.packageStateController = packageStateController;
        this.appStateProvider = appStateProvider;
        this.packages = Sets.newHashSet(packages);
        this.action = action;
    }

    public PackageStateUpdateAsyncTask withProgressDialog(Dialog progressDialog) {
        this.progressDialog = progressDialog;
        return this;
    }

    public PackageStateUpdateAsyncTask withEndingToast(Toast endingToast) {
        this.endingToast = endingToast;
        return this;
    }

    public PackageStateUpdateAsyncTask withManualStateUpdateSent(final ManualStateUpdateEventManager manualStateUpdateEventManager) {
        this.manualStateUpdateEventManager = manualStateUpdateEventManager;
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
        log.debug("Actioned (" + action + ") packages: " + actionedPackages);
    }

    @Override
    protected Collection<String> doInBackground(Void... params) {
        if (Action.ENABLE.equals(action)) {
            Set<String> needActionPackages = getSingleActionPackages(Action.ENABLE, packages);
            notifyManualStateUpdate(needActionPackages, true);
            packageStateController.enablePackages(needActionPackages);
            return needActionPackages;
        } else if (Action.DISABLE.equals(action)) {
            Set<String> needActionPackages = getSingleActionPackages(Action.DISABLE, packages);
            notifyManualStateUpdate(needActionPackages, false);
            packageStateController.disablePackages(needActionPackages);
            return needActionPackages;
        } else if (Action.TOGGLE.equals(action)) {
            Set<String> packagesToEnable = getSingleActionPackages(Action.ENABLE, packages);
            notifyManualStateUpdate(packagesToEnable, true);
            packageStateController.enablePackages(packagesToEnable);

            Set<String> otherPackages = Sets.difference(packages, packagesToEnable);
            Set<String> packagesToDisable = getSingleActionPackages(Action.DISABLE, otherPackages);
            notifyManualStateUpdate(packagesToDisable, false);
            packageStateController.disablePackages(packagesToDisable);
            return Sets.union(packagesToEnable, packagesToDisable);
        } else {
            throw new IllegalArgumentException("Unsupported action: " + action);
        }
    }

    private Set<String> getSingleActionPackages(Action action, Set<String> packageNames) {
        if (action != Action.ENABLE && action != Action.DISABLE) {
            throw new IllegalStateException("Unsupported action " + action + " in getting single action");
        }
        final boolean targetState = Action.ENABLE.equals(action) ? true : false;

        return stream(packageNames)
                .filter(packageName -> targetState ^ appStateProvider.isPackageEnabled(packageName))
                .collect(Collectors.toSet());
    }

    private void notifyManualStateUpdate(Collection<String> packageNames, boolean enable) {
        if (manualStateUpdateEventManager == null) {
            return;
        }
        PackageState packageState = enable ? PackageState.ENABLE : PackageState.DISABLE;
        for (String packageName : packageNames) {
            manualStateUpdateEventManager.publishUpdate(packageName, packageState);
        }
    }
}
