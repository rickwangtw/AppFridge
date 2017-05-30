package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.databinding.BaseObservable;

import com.google.common.base.Objects;

import java8.util.function.Consumer;
import lombok.Builder;

@Builder
public class AppGroupViewModel extends BaseObservable {

    private final String appGroupName;
    private final boolean isVirtualGroup;

    private Consumer<String> appGroupDeletingConsumer;
    private boolean showAppGroupConfigButtons = false;

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
        if (appGroupDeletingConsumer != null) {
            appGroupDeletingConsumer.accept(appGroupName);
        }
    }

    public boolean isShowAppGroupConfigButtons() {
        return this.showAppGroupConfigButtons;
    }

    public void setShowAppGroupConfigButtons(boolean showAppGroupConfigButtons) {
        this.showAppGroupConfigButtons = showAppGroupConfigButtons;

        notifyChange();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppGroupViewModel that = (AppGroupViewModel) o;
        return Objects.equal(appGroupName, that.appGroupName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(appGroupName);
    }
}
