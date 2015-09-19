package com.mysticwind.disabledappmanager.domain;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Set;

public interface PackageListProvider {
    Set<AppInfo> getPackages();
    List<AppInfo> getOrderedPackages();
}
