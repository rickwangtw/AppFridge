package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;

import java8.util.function.Consumer;
import java8.util.function.Supplier;
import lombok.Builder;

// Set and get methods needs to declared explicitly for Data binding to work.
@Builder
public class ApplicationModel extends BaseObservable {

    // if package assets are absent, we use this supplier to get it
    private final Supplier<PackageAssets> applicationAssetSupplier;
    private final Consumer<String> applicationLauncher;

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

        change();
    }

    public void setApplicationLabel(String applicationLabel) {
        this.applicationLabel = applicationLabel;

        change();
    }

    public void setApplicationIcon(Drawable applicationIcon) {
        this.applicationIcon = applicationIcon;

        change();
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;

        change();
    }

    public void setSelected(boolean selected) {
        this.selected = selected;

        change();
    }

    public void setPackageAssets(PackageAssets packageAssets) {
        this.packageAssets = packageAssets;

        this.applicationLabel = packageAssets.getAppName();
        this.applicationIcon = packageAssets.getIconDrawable();

        change();
    }

    private void change() {
        notifyChange();
    }

    public void launchApplication() {
        applicationLauncher.accept(packageName);
    }

    public void changeAppStateChangeViewClicked(View view) {
        // TODO
    }

    public void removeFromAppGroup(String appGroupName) {
        // TODO
    }
}
