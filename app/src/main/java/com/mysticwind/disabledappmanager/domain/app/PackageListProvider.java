package com.mysticwind.disabledappmanager.domain.app;

import com.mysticwind.disabledappmanager.domain.app.model.ApplicationFilter;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Set;

import java8.util.Optional;

public interface PackageListProvider {
    Optional<AppInfo> getPackage(String packageName);

    Set<AppInfo> getPackages();
    Set<AppInfo> getPackages(ApplicationFilter applicationFilter);

    List<AppInfo> getOrderedPackages(ApplicationOrderingMethod orderingMethod);
    List<AppInfo> getOrderedPackages(ApplicationFilter applicationFilter, ApplicationOrderingMethod orderingMethod);
}
