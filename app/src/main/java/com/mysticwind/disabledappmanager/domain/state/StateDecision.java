package com.mysticwind.disabledappmanager.domain.state;

public class StateDecision {
    private final String packageName;
    private final PackageState decidedState;

    public StateDecision(String packageName, PackageState decidedState) {
        this.packageName = packageName;
        this.decidedState = decidedState;
    }

    public String getPackageName() {
        return packageName;
    }

    public PackageState getDecidedState() {
        return decidedState;
    }
}
