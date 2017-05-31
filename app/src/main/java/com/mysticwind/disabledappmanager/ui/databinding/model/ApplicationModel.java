package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.Switch;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;
import com.mysticwind.library.search.model.FilterableFields;

import java.util.List;

import java8.util.function.BiConsumer;
import java8.util.function.Consumer;
import java8.util.function.Supplier;
import lombok.Builder;

// Set and get methods needs to declared explicitly for Data binding to work.
@Builder
public class ApplicationModel extends BaseObservable implements FilterableFields {

    // if package assets are absent, we use this supplier to get it
    private final Supplier<PackageAssets> applicationAssetSupplier;
    private final Consumer<String> applicationLauncher;
    private final BiConsumer<String, Boolean> packageStatusChangeConsumer;
    private final BiConsumer<String, String> appGroupPackageRemovingConsumer;

    private String packageName;
    private String applicationLabel;
    private Drawable applicationIcon;
    private boolean isEnabled;
    private boolean selected = false;
    private PackageAssets packageAssets;

    public String getPackageName() {
        return this.packageName;
    }

    public Drawable getApplicationIcon() {
        if (packageAssets == null) {
            packageAssets = applicationAssetSupplier.get();
            setPackageAssets(packageAssets);
        }
        return this.applicationIcon;
    }

    public String getApplicationLabel() {
        if (packageAssets == null) {
            packageAssets = applicationAssetSupplier.get();
            setPackageAssets(packageAssets);
        }
        return this.applicationLabel;
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }

    public boolean isSelected() {
        return this.selected;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;

        notifyChange();
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;

        notifyChange();
    }

    public void setApplicationIcon(Drawable applicationIcon) {
        this.applicationIcon = applicationIcon;

        notifyChange();
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;

        notifyChange();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        notifyChange();
    }

    public void setPackageAssets(final PackageAssets packageAssets) {
        this.packageAssets = packageAssets;

        this.applicationLabel = packageAssets.getAppName();
        this.applicationIcon = packageAssets.getIconDrawable();

        notifyChange();
    }

    public void launchApplication() {
        applicationLauncher.accept(packageName);
    }

    public void changeAppStateChangeViewClicked(View view) {
        Switch switchView = (Switch) view;
        if (switchView.isChecked() == isEnabled) {
            return;
        }
        if (packageStatusChangeConsumer != null) {
            packageStatusChangeConsumer.accept(packageName, switchView.isChecked());
        }
    }

    public void removeFromAppGroup(String appGroupName) {
        if (appGroupPackageRemovingConsumer != null) {
            appGroupPackageRemovingConsumer.accept(appGroupName, packageName);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ApplicationModel that = (ApplicationModel) o;
        return Objects.equal(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(packageName);
    }

    @Override
    public List<String> getSearchableStringsOrderedByPriority() {
        return ImmutableList.of(
                applicationLabel,
                packageName
        );
    }
}
