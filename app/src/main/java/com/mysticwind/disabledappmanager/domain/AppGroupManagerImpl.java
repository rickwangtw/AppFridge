package com.mysticwind.disabledappmanager.domain;

import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;

import java.util.Collection;
import java.util.Set;

public class AppGroupManagerImpl implements AppGroupManager {
    private static final String TAG = "AppGroupManagerImpl";

    private final AppGroupDAO appGroupDAO;

    public AppGroupManagerImpl(AppGroupDAO appGroupDAO) {
        this.appGroupDAO = appGroupDAO;
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
        appGroupDAO.addPackagesToAppGroup(packageNames, appGroupName);
    }

    @Override
    public void deleteAppGroup(String appGroupName) {
        appGroupDAO.deleteAppGroup(appGroupName);
    }

    @Override
    public void deletePackageFromAppGroup(String packageName, String appGroupName) {
        appGroupDAO.deletePackageFromAppGroup(packageName, appGroupName);
    }
}
