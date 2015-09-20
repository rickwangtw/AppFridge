package com.mysticwind.disabledappmanager.domain;

import java.util.Collection;

public interface PackageStateController {
    void enablePackages(Collection<String> packageNames);
    void disablePackages(Collection<String> packageNames);
}
