package com.mysticwind.disabledappmanager;

import android.content.pm.ApplicationInfo;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppSelectedListener implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    private static final String TAG ="AppSelectedListener";

    private final List<ApplicationInfo> packages;
    private final Set<String> selectedPackageNames = new HashSet<>();

    public AppSelectedListener(List<ApplicationInfo> packages) {
        this.packages = packages;
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

    @Override
    public void onClick(View v) {
    }
}
