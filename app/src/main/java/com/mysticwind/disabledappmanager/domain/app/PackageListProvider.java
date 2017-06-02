package com.mysticwind.disabledappmanager.domain.app;

import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;

import java.util.List;
import java.util.Set;

public interface PackageListProvider {
    Set<AppInfo> getPackages();
    List<AppInfo> getOrderedPackages(ApplicationOrderingMethod orderingMethod);
}
