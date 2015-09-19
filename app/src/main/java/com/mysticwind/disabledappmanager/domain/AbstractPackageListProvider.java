package com.mysticwind.disabledappmanager.domain;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public abstract class AbstractPackageListProvider implements PackageListProvider {
    private static final String TAG = "AbstractPackageListProvider";

    @Override
    public List<AppInfo> getOrderedPackages() {
        return packageListOrderedByPackageName(getPackages());
    }

    private List<AppInfo> packageListOrderedByPackageName(Collection<AppInfo> packages) {
        List<AppInfo> orderedPackageList = new ArrayList<>(packages);
        Collections.sort(orderedPackageList, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo lhs, AppInfo rhs) {
                return lhs.getPackageName().compareTo(rhs.getPackageName());
            }
        });
        return orderedPackageList;
    }
}
