package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.app.Dialog;
import android.databinding.BaseObservable;
import android.databinding.ObservableArrayList;

import com.google.common.base.Preconditions;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdate;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateListener;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssets;
import com.mysticwind.disabledappmanager.domain.model.AppInfo;
import com.mysticwind.disabledappmanager.domain.state.PackageState;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdate;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateListener;
import com.mysticwind.disabledappmanager.ui.common.PackageStateUpdateAsyncTask;

import java.util.List;
import java.util.Set;

import java8.util.Optional;
import java8.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;

import static java8.util.stream.StreamSupport.stream;

@Slf4j
public class ApplicationModelList extends BaseObservable implements AppAssetUpdateListener {

    // the numbers must be consistent with the order of the strings
    public static class ViewMode {
        public static final int ALL = 0;
        public static final int ENABLED = 1;
        public static final int DISABLED = 2;
    }

    private final ObservableArrayList<ApplicationModel> applicationModelList = new ObservableArrayList<>();
    private final PackageListProvider packageListProvider;
    private final PackageAssetService packageAssetService;
    private final PackageStateController packageStateController;
    private final AppStateProvider appStateProvider;
    private final PackageStateUpdateEventManager packageStateUpdateEventManager;
    private final AppGroupManager appGroupManager;
    private final Dialog progressDialog;
    // weak reference will be released
    private final PackageStateUpdateListener packageStateUpdateListener = new PackageStateUpdateListener() {
        @Override
        public void update(PackageStateUpdate event) {
            findApplicationModel(event.getAppGroupName())
                    .ifPresent(applicationModel -> {
                        applicationModel.setEnabled(PackageState.ENABLE.equals(event.getPackageState()));

                        switch (viewMode) {
                            case ViewMode.ENABLED:
                                applicationModel.setHidden(!applicationModel.isEnabled());
                                break;
                            case ViewMode.DISABLED:
                                applicationModel.setHidden(applicationModel.isEnabled());
                                break;
                            case ViewMode.ALL:
                            default:
                                applicationModel.setHidden(false);
                                break;
                        }
                    });
        }
    };

    private int viewMode = ViewMode.ALL;

    public ApplicationModelList(final PackageListProvider packageListProvider,
                                final PackageAssetService packageAssetService,
                                final AppAssetUpdateEventManager appAssetUpdateEventManager,
                                final PackageStateController packageStateController,
                                final AppStateProvider appStateProvider,
                                final PackageStateUpdateEventManager packageStateUpdateEventManager,
                                final AppGroupManager appGroupManager,
                                final Dialog progressDialog) {
        this.packageListProvider = Preconditions.checkNotNull(packageListProvider);
        this.packageAssetService = Preconditions.checkNotNull(packageAssetService);
        this.packageStateController = Preconditions.checkNotNull(packageStateController);
        this.appStateProvider = Preconditions.checkNotNull(appStateProvider);
        this.packageStateUpdateEventManager = Preconditions.checkNotNull(packageStateUpdateEventManager);
        this.appGroupManager = Preconditions.checkNotNull(appGroupManager);
        this.progressDialog = Preconditions.checkNotNull(progressDialog);

        appAssetUpdateEventManager.registerListener(this);
        packageStateUpdateEventManager.registerListener(packageStateUpdateListener);

        List<AppInfo> appInfoList = packageListProvider.getOrderedPackages();
        for (AppInfo app : appInfoList) {
            // this is to allocate a request to get package assets that we will obtain from AppAssetUpdateEventManager
            PackageAssets packageAsset = packageAssetService.getPackageAssets(app.getPackageName());
            applicationModelList.add(ApplicationModel.builder()
                    .packageName(app.getPackageName())
                    // the package asset is only available when provided from AppAssetUpdateEventManager
                    // this is to prioritize UI requests for package assets
                    .applicationAssetSupplier(() -> packageAssetService.getPackageAssets(app.getPackageName()))
                    .applicationLabel(packageAsset.getAppName())
                    .applicationIcon(packageAsset.getIconDrawable())
                    .isEnabled(app.isEnabled())
                    .build());
        }
    }

    // Lombok this getter will fail the data binding
    public ObservableArrayList<ApplicationModel> getApplicationModelList() {
        return this.applicationModelList;
    }

    public int getViewMode() {
        return viewMode;
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;

        for (ApplicationModel applicationModel : applicationModelList) {
            switch (viewMode) {
                case ViewMode.ENABLED:
                    applicationModel.setHidden(!applicationModel.isEnabled());
                    break;
                case ViewMode.DISABLED:
                    applicationModel.setHidden(applicationModel.isEnabled());
                    break;
                case ViewMode.ALL:
                default:
                    applicationModel.setHidden(false);
                    break;
            }
        }
        notifyChange();
    }

    @Override
    public void update(AppAssetUpdate event) {
        String packageName = event.getPackageName();

        findApplicationModel(packageName)
                .ifPresent(applicationModel -> updatePackageAsset(applicationModel));
    }

    private Optional<ApplicationModel> findApplicationModel(String packageName) {
        return stream(applicationModelList)
                .filter(applicationModel -> packageName.equals(applicationModel.getPackageName()))
                .findFirst();
    }

    private void updatePackageAsset(ApplicationModel applicationModel) {
        PackageAssets packageAsset = packageAssetService.getPackageAssets(applicationModel.getPackageName());

        applicationModel.setPackageAssets(packageAsset);
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

    private Set<ApplicationModel> getSelectedApplications() {
        return stream(applicationModelList)
                .filter(applicationModel -> applicationModel.isSelected())
                .collect(Collectors.toSet());
    }

    private void clearSelectedApplications() {
        getSelectedApplications()
                .forEach(applicationModel -> applicationModel.setSelected(false));
    }


}
