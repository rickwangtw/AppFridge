package com.mysticwind.disabledappmanager.domain;

import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;

public class RootProcessPackageStateController implements PackageStateController {
    private static final String TAG = "RootPStateController";
    private static final String PACKAGE_COMMAND_FORMAT = "pm %s %s \n";

    @Override
    public void enablePackages(Set<String> packageNames) {
        modifyPackageStatesToStateSafely(packageNames, true);
    }

    @Override
    public void disablePackages(Set<String> packageNames) {
        modifyPackageStatesToStateSafely(packageNames, false);
    }

    private void modifyPackageStatesToStateSafely(Set<String> packageNames, boolean state) {
        try {
            modifyPackageStatesToState(packageNames, state);
        } catch (Exception e) {
            Log.w(TAG, "Error occurred on modifying (" + state + ") packages: " + packageNames, e);
        }
    }

    private void modifyPackageStatesToState(Set<String> packageNames, boolean state) throws IOException {
        Process process = Runtime.getRuntime().exec("su");
        DataOutputStream outputStream = new DataOutputStream(process.getOutputStream());

        for (String packageName : packageNames) {
            Log.d(TAG, "Disabling package: " + packageName);
            outputStream.writeBytes(buildCommand(packageName, state));
            outputStream.flush();
        }

        outputStream.close();
    }

    private String buildCommand(String packageName, boolean state) {
        return String.format(PACKAGE_COMMAND_FORMAT, state ? "enable" : "disable", packageName);
    }
}
