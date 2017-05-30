package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.databinding.BaseObservable;

import com.google.common.base.Preconditions;

public class AppGroupViewModel extends BaseObservable {

    private final String appGroupName;
    private final boolean isVirtualGroup;

    private boolean showAppGroupConfigButtons = false;

    public AppGroupViewModel(final String appGroupName, final boolean isVirtualGroup) {
        this.appGroupName = Preconditions.checkNotNull(appGroupName);
        this.isVirtualGroup = isVirtualGroup;
    }

    public String getAppGroupName() {
        return this.appGroupName;
    }

    public boolean isVirtualGroup() {
        return this.isVirtualGroup;
    }

    public void showGroupActionDialog() {
        if (isVirtualGroup) {
            return;
        }
        // TODO
    }

    public void showAddPackagesToAppGroupDialog() {
        if (isVirtualGroup) {
            return;
        }
        // TODO
    }

    public void removeAppGroup() {
        if (isVirtualGroup) {
            return;
        }
        // TODO
    }

    public boolean isShowAppGroupConfigButtons() {
        return this.showAppGroupConfigButtons;
    }

    public void setShowAppGroupConfigButtons(boolean showAppGroupConfigButtons) {
        this.showAppGroupConfigButtons = showAppGroupConfigButtons;

        notifyChange();
    }
}
