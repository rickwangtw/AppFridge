package com.mysticwind.disabledappmanager.domain;

import java.util.Collection;
import java.util.Set;

public interface AppGroupManager {
    Set<String> getAllAppGroups();
    Set<String> getPackagesOfAppGroup(String appGroupName);
    void addPackagesToAppGroup(Collection<String> packageNames, String appGroupName);
    void deleteAppGroup(String appGroupName);
    void deletePackageFromAppGroup(String packageName, String appGroupName);
}
