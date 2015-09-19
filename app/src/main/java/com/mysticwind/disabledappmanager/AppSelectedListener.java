package com.mysticwind.disabledappmanager;

import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import com.mysticwind.disabledappmanager.domain.PackageStateController;

import java.util.HashSet;
import java.util.Set;

public class AppSelectedListener implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG ="AppSelectedListener";

    private final PackageStateController packageStateController;
    private final Set<String> selectedPackageNames = new HashSet<>();

    public AppSelectedListener(PackageStateController packageStateController) {
        this.packageStateController = packageStateController;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        String packageName = (String) buttonView.getTag();
        if (isChecked) {
            selectedPackageNames.add(packageName);
        } else {
            selectedPackageNames.remove(packageName);
        }
    }

    public boolean isPackageNameSelected(String packageName) {
        return selectedPackageNames.contains(packageName);
    }

    @Override
    public void onClick(View v) {
        packageStateController.disablePackages(selectedPackageNames);
    }
}
