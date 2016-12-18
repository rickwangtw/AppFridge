package com.mysticwind.disabledappmanager.domain;

import android.util.Log;

import com.google.common.collect.ImmutableSet;
import com.mysticwind.disabledappmanager.domain.state.PackageState;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;

import eu.chainfire.libsuperuser.Shell;

public class RootProcessPackageStateController implements PackageStateController {
    private static final String TAG = "RootPStateController";
    private static final String PACKAGE_COMMAND_FORMAT = "pm %s %s \n";

    private final PackageStateUpdateEventManager packageStateUpdateEventManager;

    public RootProcessPackageStateController(
            PackageStateUpdateEventManager packageStateUpdateEventManager) {
        this.packageStateUpdateEventManager = packageStateUpdateEventManager;
    }

    @Override
    public void enablePackages(Collection<String> packageNames) {
        modifyPackageStatesToStateSafely(packageNames, true);
    }

    @Override
    public void disablePackages(Collection<String> packageNames) {
        modifyPackageStatesToStateSafely(packageNames, false);
    }

    private void modifyPackageStatesToStateSafely(Collection<String> packageNames, boolean state) {
        try {
            Set<String> packageNameSet = ImmutableSet.copyOf(packageNames);
            modifyPackageStatesToState(packageNameSet, state);
            for (String packageName : packageNameSet) {
                PackageState packageState = state ? PackageState.ENABLE : PackageState.DISABLE;
                packageStateUpdateEventManager.publishUpdate(packageName, packageState);
            }
        } catch (Exception e) {
            Log.w(TAG, "Error occurred on modifying (" + state + ") packages: " + packageNames, e);
        }
    }

    private void modifyPackageStatesToState(Set<String> packageNames, boolean state) throws IOException {
        for (String packageName : packageNames) {
            Log.d(TAG, "Modifying (" + state + ") package: " + packageName);
            Shell.SU.run(buildCommand(packageName, state));
        }
    }

    private String buildCommand(String packageName, boolean state) {
        return String.format(PACKAGE_COMMAND_FORMAT, state ? "enable" : "disable", packageName);
    }
}
