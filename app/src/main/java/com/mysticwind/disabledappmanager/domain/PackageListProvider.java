package com.mysticwind.disabledappmanager.domain;

import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Set;

public interface PackageListProvider {
    Set<AppInfo> getAllPackages();
    List<AppInfo> getOrderedAllPackages();
    Set<AppInfo> getEnabledPackages();
    List<AppInfo> getOrderedEnabledPackages();
    Set<AppInfo> getDisabledPackages();
    List<AppInfo> getOrderedDisabledPackages();
}
