package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.app.Dialog;
import android.content.Context;
import android.databinding.BaseObservable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.app.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.app.model.ApplicationOrderingMethod;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;
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
    private final RxDataSource<ApplicationModel> rxDataSource;
    private final PackageListProvider packageListProvider;
    private final PackageAssetService packageAssetService;
    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
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

    @Setter
    private Dialog addToAppGroupDialog;

    public ApplicationStateViewModel(final Context context,
                                     final RxDataSource<ApplicationModel> dataSource,
                                     final PackageListProvider packageListProvider,
                                     final PackageAssetService packageAssetService,
                                     final AppAssetUpdateEventManager appAssetUpdateEventManager,
                                     final PackageStateController packageStateController,
                                     final AppStateProvider appStateProvider,
                                     final PackageStateUpdateEventManager packageStateUpdateEventManager,
                                     final Dialog progressDialog,
                                     final AppLauncher appLauncher) {
        this.context = Preconditions.checkNotNull(context);
        this.rxDataSource = Preconditions.checkNotNull(dataSource);
        this.packageListProvider = Preconditions.checkNotNull(packageListProvider);
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.packageStateController = Preconditions.checkNotNull(packageStateController);
        this.appStateProvider = Preconditions.checkNotNull(appStateProvider);
        Preconditions.checkNotNull(packageStateUpdateEventManager);
        this.progressDialog = Preconditions.checkNotNull(progressDialog);
        this.appLauncher = Preconditions.checkNotNull(appLauncher);

        appAssetUpdateEventManager.registerListener(appAssetupUpdateListener);
        packageStateUpdateEventManager.registerListener(packageStateUpdateListener);

        reloadAdapter();
    }

    public int getViewMode() {
        return viewMode;
    }

    public void setViewMode(int viewMode) {
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
            updateApplicationModelCache();
        }

        List<ApplicationModel> filteredApplicationModelList = stream(getCachedApplicationModels())
                .filter(applicationModel -> shouldIncludePackageInAdapter(applicationModel, viewMode))
                .filter(applicationModel -> applicationModelPredicate.test(applicationModel))
                .sorted(
                        (applicationModel1, applicationModel2) ->
                                applicationModel1.getApplicationLabel().compareTo(applicationModel2.getApplicationLabel()))
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


    private void updateApplicationModelCache() {
        cachedApplicationModels = stream(packageListProvider.getOrderedPackages(ApplicationOrderingMethod.APPLICATION_LABEL))
                .map(appInfo -> {
                    PackageAssets packageAsset = packageAssetService.getPackageAssets(appInfo.getPackageName());
                    return ApplicationModel.builder()
                            .packageName(appInfo.getPackageName())
                            .applicationLabel(packageAsset.getAppName())
                            .applicationIcon(packageAsset.getIconDrawable())
                            .isEnabled(appInfo.isEnabled())
                            .selected(cachedSelectedPackageNames.contains(appInfo.getPackageName()))
                            .applicationLauncher(
                                    packageName ->
                                            appLauncher.launch(context, packageName))
                            .build();
                })
                .collect(Collectors.toList());
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
}
