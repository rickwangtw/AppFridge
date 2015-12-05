package com.mysticwind.disabledappmanager.domain;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupOperation;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class AppGroupManagerImpl implements AppGroupManager {
    private static final String TAG = "AppGroupManagerImpl";

    private final AppGroupDAO appGroupDAO;
    private final AppGroupUpdateEventManager appGroupUpdateEventManager;
    private final PackageListProvider packageListProvider;

    public AppGroupManagerImpl(AppGroupDAO appGroupDAO,
                               AppGroupUpdateEventManager appGroupUpdateEventManager,
                               PackageListProvider packageListProvider) {
        this.appGroupDAO = appGroupDAO;
        this.appGroupUpdateEventManager = appGroupUpdateEventManager;
        this.packageListProvider = packageListProvider;
    }

    @Override
    public Set<String> getAllAppGroups() {
        return appGroupDAO.getAllAppGroups();
    }

    @Override
    public Set<String> getPackagesOfAppGroup(String appGroupName) {
        return filterOutUninstalledPackageNames(appGroupDAO.getPackagesOfAppGroup(appGroupName));
    }

    private Set<String> filterOutUninstalledPackageNames(Set<String> packageNames) {
        Set<String> installedPackageNameSet = new HashSet<>();
        for (AppInfo appInfo : packageListProvider.getPackages()) {
            installedPackageNameSet.add(appInfo.getPackageName());
        }

        return Sets.intersection(packageNames, installedPackageNameSet);
    }

    @Override
    public void addPackagesToAppGroup(Collection<String> packageNames, String appGroupName) {
        boolean newAppGroupCreated = false;
        if (!getAllAppGroups().contains(appGroupName)) {
            newAppGroupCreated = true;
        }
        appGroupDAO.addPackagesToAppGroup(packageNames, appGroupName);
        if (newAppGroupCreated) {
            appGroupUpdateEventManager.publishUpdate(appGroupName, AppGroupOperation.ADD);
        }
        appGroupUpdateEventManager.publishUpdate(appGroupName, AppGroupOperation.PACKAGE_ADDED);
    }

    @Override
    public void deleteAppGroup(String appGroupName) {
        appGroupDAO.deleteAppGroup(appGroupName);
        appGroupUpdateEventManager.publishUpdate(appGroupName, AppGroupOperation.DELETE);
    }

    @Override
    public void deletePackageFromAppGroup(String packageName, String appGroupName) {
        appGroupDAO.deletePackageFromAppGroup(packageName, appGroupName);
        appGroupUpdateEventManager.publishUpdate(appGroupName, AppGroupOperation.PACKAGE_REMOVED);
    }
}
