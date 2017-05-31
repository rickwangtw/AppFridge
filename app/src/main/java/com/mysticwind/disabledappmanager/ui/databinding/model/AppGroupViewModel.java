package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.databinding.BaseObservable;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.mysticwind.disabledappmanager.ui.activity.perspective.FilterableFields;

import java.util.List;

import java8.util.function.Consumer;
import lombok.Builder;

@Builder
public class AppGroupViewModel extends BaseObservable implements FilterableFields {

    private final String appGroupName;
    private final boolean isVirtualGroup;
    private Consumer<String> appGroupPackageFreezingConsumer;
    private Consumer<String> appGroupPackageUnfreezingConsumer;
    private Consumer<String> appGroupDeletingConsumer;
    private Consumer<String> appGroupPackageAddingConsumer;

    public String getAppGroupName() {
        return this.appGroupName;
    }

    public boolean isVirtualGroup() {
        return this.isVirtualGroup;
    }

    public void showAddPackagesToAppGroupDialog() {
        if (isVirtualGroup) {
            return;
        }
        if (appGroupPackageAddingConsumer != null) {
            appGroupPackageAddingConsumer.accept(appGroupName);
        }
    }

    public void removeAppGroup() {
        if (isVirtualGroup) {
            return;
        }
        if (appGroupDeletingConsumer != null) {
            appGroupDeletingConsumer.accept(appGroupName);
        }
    }

    public void freezePackagesOfAppGroup() {
        if (isVirtualGroup) {
            return;
        }
        if (appGroupPackageFreezingConsumer != null) {
            appGroupPackageFreezingConsumer.accept(appGroupName);
        }
    }

    public void unfreezePackagesOfAppGroup() {
        if (isVirtualGroup) {
            return;
        }
        if (appGroupPackageUnfreezingConsumer != null) {
            appGroupPackageUnfreezingConsumer.accept(appGroupName);
        }
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

    @Override
    public List<String> getSearchableStringsOrderedByPriority() {
        return ImmutableList.of(
                appGroupName
        );
    }
}
