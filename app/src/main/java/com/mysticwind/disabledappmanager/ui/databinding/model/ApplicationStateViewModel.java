package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.app.Dialog;
import android.content.Context;
import android.databinding.BaseObservable;
import android.graphics.drawable.Drawable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.app.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationFilter;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageState;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdate;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateListener;
import com.mysticwind.disabledappmanager.ui.common.PackageStateUpdateAsyncTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java8.util.Optional;
import java8.util.function.Predicate;
import java8.util.function.Supplier;
import java8.util.stream.Collectors;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class ApplicationStateViewModel extends BaseObservable {

    // the numbers must be consistent with the order of the strings
    public static class ViewMode {
        public static final int ALL = 0;
        public static final int ENABLED = 1;
        public static final int DISABLED = 2;
    }

    // in search mode, the selection status will be discarded when reloading the adapter
    private final Set<String> cachedSelectedPackageNames = new HashSet<>();

    private final Context context;
    private final Drawable defaultIcon;
    private final RxDataSource<ApplicationModel> rxDataSource;
    private final PackageListProvider packageListProvider;
    private final PackageAssetService packageAssetService;
    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final ManualStateUpdateEventManager manualStateUpdateEventManager;
    private final Dialog progressDialog;
    private final AppLauncher appLauncher;
    // weak reference will be released
    private final PackageStateUpdateListener packageStateUpdateListener = new PackageStateUpdateListener() {
        @Override
        public void update(PackageStateUpdate event) {
            findApplicationModel(event.getAppGroupName())
                    .ifPresent(applicationModel -> {
                        applicationModel.setEnabled(PackageState.ENABLE.equals(event.getPackageState()));
                        reloadAdapter();
                    });
        }
    };
    // weak reference will be released
    private final AppAssetUpdateListener appAssetupUpdateListener = new AppAssetUpdateListener() {
        @Override
        public void update(AppAssetUpdate event) {
            String packageName = event.getPackageName();

            findApplicationModel(packageName)
                    .ifPresent(applicationModel -> {
                        PackageAssets packageAsset = packageAssetService.getPackageAssets(applicationModel.getPackageName());
                        applicationModel.setPackageAssets(packageAsset);
                    });
        }
    };

    private int viewMode = ViewMode.ALL;
    private List<ApplicationModel> cachedApplicationModels = Lists.newArrayList();
    private boolean showSystemApps;
    private ApplicationOrderingMethod orderingMethod;

    @Setter
    private Dialog addToAppGroupDialog;

    /* constructor for first launch optimization */
    public ApplicationStateViewModel(final Context context,
                                     final Drawable defaultIcon,
                                     final RxDataSource<ApplicationModel> dataSource,
                                     final PackageListProvider packageListProvider,
                                     final PackageAssetService packageAssetService,
                                     final AppAssetUpdateEventManager appAssetUpdateEventManager,
                                     final PackageStateController packageStateController,
                                     final AppStateProvider appStateProvider,
                                     final PackageStateUpdateEventManager packageStateUpdateEventManager,
                                     final ManualStateUpdateEventManager manualStateUpdateEventManager,
                                     final Dialog progressDialog,
                                     final AppLauncher appLauncher) {
        this(context, defaultIcon, dataSource, packageListProvider, packageAssetService, appAssetUpdateEventManager,
                packageStateController, appStateProvider, packageStateUpdateEventManager, manualStateUpdateEventManager,
                progressDialog, appLauncher, false, ApplicationOrderingMethod.PACKAGE_NAME, true);
    }

    public ApplicationStateViewModel(final Context context,
                                     final Drawable defaultIcon,
                                     final RxDataSource<ApplicationModel> dataSource,
                                     final PackageListProvider packageListProvider,
                                     final PackageAssetService packageAssetService,
                                     final AppAssetUpdateEventManager appAssetUpdateEventManager,
                                     final PackageStateController packageStateController,
                                     final AppStateProvider appStateProvider,
                                     final PackageStateUpdateEventManager packageStateUpdateEventManager,
                                     final ManualStateUpdateEventManager manualStateUpdateEventManager,
                                     final Dialog progressDialog,
                                     final AppLauncher appLauncher,
                                     final boolean showSystemApps,
                                     final ApplicationOrderingMethod orderingMethod) {
        this(context, defaultIcon, dataSource, packageListProvider, packageAssetService, appAssetUpdateEventManager,
                packageStateController, appStateProvider, packageStateUpdateEventManager, manualStateUpdateEventManager,
                progressDialog, appLauncher, showSystemApps, orderingMethod, false);
    }

    public ApplicationStateViewModel(final Context context,
                                     final Drawable defaultIcon,
                                     final RxDataSource<ApplicationModel> dataSource,
                                     final PackageListProvider packageListProvider,
                                     final PackageAssetService packageAssetService,
                                     final AppAssetUpdateEventManager appAssetUpdateEventManager,
                                     final PackageStateController packageStateController,
                                     final AppStateProvider appStateProvider,
                                     final PackageStateUpdateEventManager packageStateUpdateEventManager,
                                     final ManualStateUpdateEventManager manualStateUpdateEventManager,
                                     final Dialog progressDialog,
                                     final AppLauncher appLauncher,
                                     final boolean showSystemApps,
                                     final ApplicationOrderingMethod orderingMethod,
                                     final boolean optimizeFirstLaunch) {
        this.context = Preconditions.checkNotNull(context);
        this.defaultIcon = Preconditions.checkNotNull(defaultIcon);
        this.rxDataSource = Preconditions.checkNotNull(dataSource);
        this.packageListProvider = Preconditions.checkNotNull(packageListProvider);
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.packageStateController = Preconditions.checkNotNull(packageStateController);
        this.appStateProvider = Preconditions.checkNotNull(appStateProvider);
        Preconditions.checkNotNull(packageStateUpdateEventManager);
        this.manualStateUpdateEventManager = Preconditions.checkNotNull(manualStateUpdateEventManager);
        this.progressDialog = Preconditions.checkNotNull(progressDialog);
        this.appLauncher = Preconditions.checkNotNull(appLauncher);
        this.showSystemApps = showSystemApps;
        this.orderingMethod = Preconditions.checkNotNull(orderingMethod);

        appAssetUpdateEventManager.registerListener(appAssetupUpdateListener);
        packageStateUpdateEventManager.registerListener(packageStateUpdateListener);

        if (optimizeFirstLaunch) {
            updateApplicationModelCache(true);
            updateDataSet(applicationModel -> true, false);
        } else {
            reloadAdapter();
        }
    }

    public int getViewMode() {
        return viewMode;
    }

    public void setViewMode(int viewMode) {
        if (this.viewMode == viewMode) {
            return;
        }
        this.viewMode = viewMode;

        clearSelectedApplications();
        reloadAdapter();
    }

    private Optional<ApplicationModel> findApplicationModel(String packageName) {
        return stream(getCachedApplicationModels())
                .filter(applicationModel -> packageName.equals(applicationModel.getPackageName()))
                .findFirst();
    }

    private List<ApplicationModel> getCachedApplicationModels() {
        return cachedApplicationModels;
    }

    public void toggleSelectedApplications() {
        updateSelectedApplications(PackageStateUpdateAsyncTask.Action.TOGGLE);
    }

    public void enableSelectedApplications() {
        updateSelectedApplications(PackageStateUpdateAsyncTask.Action.ENABLE);
    }

    public void disableSelectedApplications() {
        updateSelectedApplications(PackageStateUpdateAsyncTask.Action.DISABLE);
    }

    private void updateSelectedApplications(PackageStateUpdateAsyncTask.Action action) {
        Set<String> packageNames = stream(getSelectedApplications())
                .map(applicationModel -> applicationModel.getPackageName())
                .collect(Collectors.toSet());

        new PackageStateUpdateAsyncTask(packageStateController,
                appStateProvider, packageNames, action)
                .withProgressDialog(progressDialog)
                .withManualStateUpdateSent(manualStateUpdateEventManager)
                .execute();

        clearSelectedApplications();
    }

    public Supplier<Set<String>> getSelectedPackageNamesSupplier() {
        return () ->
                stream(getSelectedApplications())
                        .map(appInfo -> appInfo.getPackageName())
                        .collect(Collectors.toSet());
    }

    public Runnable getClearSelectedPackagesRunnable() {
        return () -> clearSelectedApplications();
    }


    public Set<ApplicationModel> getSelectedApplications() {
        return stream(getCachedApplicationModels())
                .filter(applicationModel ->
                        applicationModel.isSelected() ||
                                cachedSelectedPackageNames.contains(applicationModel.getPackageName())
                )
                .collect(Collectors.toSet());
    }

    private void clearSelectedApplications() {
        cachedSelectedPackageNames.clear();
        stream(getSelectedApplications())
                .forEach(applicationModel -> applicationModel.setSelected(false));
    }

    private void reloadAdapter() {
        updateDataSet(applicationModel -> true, true);
    }

    private void updateDataSet(final Predicate<ApplicationModel> applicationModelPredicate,
                               final boolean fullReload) {
        cacheSelectedPackages();

        if (fullReload) {
            updateApplicationModelCache(false);
        }

        List<ApplicationModel> filteredApplicationModelList = stream(getCachedApplicationModels())
                .filter(applicationModel -> shouldIncludePackageInAdapter(applicationModel, viewMode))
                .filter(applicationModel -> applicationModelPredicate.test(applicationModel))
                .collect(Collectors.toList());

        rxDataSource
                .updateDataSet(filteredApplicationModelList)
                .updateAdapter();
    }

    private void cacheSelectedPackages() {
        stream(getSelectedApplications())
                .map(applicationModel -> applicationModel.getPackageName())
                .forEach(
                        packageName ->
                                cachedSelectedPackageNames.add(packageName));
    }

    private void updateApplicationModelCache(final boolean optimizePackageNameAssetLoading) {
        cachedApplicationModels = stream(packageListProvider.getOrderedPackages(appFilter(), orderingMethod))
                .map(appInfo -> buildApplicationModel(appInfo, orderingMethod, optimizePackageNameAssetLoading))
                .collect(Collectors.toList());
    }

    private ApplicationModel buildApplicationModel(final AppInfo appInfo,
                                                   final ApplicationOrderingMethod orderingMethod,
                                                   final boolean optimizePackageNameAssetLoading) {
        final PackageAssets packageAssets;
        if (orderingMethod == ApplicationOrderingMethod.PACKAGE_NAME && optimizePackageNameAssetLoading) {
            packageAssets = new PackageAssets(appInfo.getPackageName(), appInfo.getPackageName(), defaultIcon);
        } else {
            packageAssets = packageAssetService.getPackageAssets(appInfo.getPackageName());
        }
        return ApplicationModel.builder()
                .packageName(appInfo.getPackageName())
                .applicationLabel(packageAssets.getAppName())
                .applicationIcon(packageAssets.getIconDrawable())
                .isEnabled(appInfo.isEnabled())
                .selected(cachedSelectedPackageNames.contains(appInfo.getPackageName()))
                .applicationLauncher(
                        packageName ->
                                appLauncher.launch(context, packageName))
                .build();
    }

    private ApplicationFilter appFilter() {
        return ApplicationFilter.builder()
                .includeSystemApp(showSystemApps ? true : false)
                .build();
    }

    public void launchAddToAppGroupDialog() {
        if (getSelectedApplications().isEmpty()) {
            return;
        }
        if (addToAppGroupDialog == null) {
            return;
        }

        addToAppGroupDialog.show();
    }

    private boolean shouldIncludePackageInAdapter(ApplicationModel applicationModel, int viewMode) {
        switch (viewMode) {
            case ViewMode.ENABLED:
                return applicationModel.isEnabled();
            case ViewMode.DISABLED:
                return !applicationModel.isEnabled();
            case ViewMode.ALL:
            default:
                return true;
        }
    }

    public void performSearch(String searchQuery) {
        final String lowCaseSearchQuery = searchQuery.toLowerCase();
        if (Strings.isNullOrEmpty(searchQuery)) {
            cancelSearch();
            return;
        }

        updateDataSet(
                applicationModel ->
                        applicationModel.getPackageName().toLowerCase().contains(lowCaseSearchQuery) ||
                                applicationModel.getApplicationLabel().toLowerCase().contains(lowCaseSearchQuery),
                false);
    }

    public void cancelSearch() {
        reloadAdapter();
    }

    public void updateViewOptions(final boolean showSystemApps,
                                  final ApplicationOrderingMethod orderingMethod) {
        if (this.showSystemApps == showSystemApps &&
                this.orderingMethod == orderingMethod) {
            return;
        }
        this.showSystemApps = showSystemApps;
        this.orderingMethod = orderingMethod;

        reloadAdapter();
    }

}
