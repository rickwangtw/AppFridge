package com.mysticwind.disabledappmanager.domain;

import java.util.Set;

public interface PackageStateController {
    void enablePackages(Set<String> packageNames);
    void disablePackages(Set<String> packageNames);
}
