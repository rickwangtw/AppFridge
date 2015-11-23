package com.mysticwind.disabledappmanager.domain;

import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupOperation;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;

import java.util.Collection;
import java.util.Set;

public class AppGroupManagerImpl implements AppGroupManager {
    private static final String TAG = "AppGroupManagerImpl";

    private final AppGroupDAO appGroupDAO;
    private final AppGroupUpdateEventManager appGroupUpdateEventManager;

    public AppGroupManagerImpl(AppGroupDAO appGroupDAO,
                               AppGroupUpdateEventManager appGroupUpdateEventManager) {
        this.appGroupDAO = appGroupDAO;
        this.appGroupUpdateEventManager = appGroupUpdateEventManager;
    }

    @Override
    public Set<String> getAllAppGroups() {
        return appGroupDAO.getAllAppGroups();
    }

    @Override
    public Set<String> getPackagesOfAppGroup(String appGroupName) {
        return appGroupDAO.getPackagesOfAppGroup(appGroupName);
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
