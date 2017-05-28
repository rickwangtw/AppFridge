package com.mysticwind.disabledappmanager.ui.databinding.model;

import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.google.common.base.Preconditions;
import com.minimize.android.rxrecycleradapter.RxDataSource;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;

import java.util.List;
import java.util.Set;

import java8.util.stream.Collectors;

import static java8.util.stream.StreamSupport.stream;

/**
 * Binding View Model for the Dialog to add Applications into an App Group.
 */
public class AddAppGroupViewModel extends BaseObservable {

    private final AppGroupManager appGroupManager;
    private final RxDataSource<ApplicationModel> dataSource;

    private List<String> appGroups;

    private boolean newAppGroupSelected;
    private String newAppGroupName;
    private int selectedAppGroupPosition;

    public AddAppGroupViewModel(final AppGroupManager appGroupManager,
                                final RxDataSource<ApplicationModel> dataSource) {
        this.appGroupManager = Preconditions.checkNotNull(appGroupManager);
        this.dataSource = Preconditions.checkNotNull(dataSource);

        reloadAppGroups();

        if (appGroups.isEmpty()) {
            newAppGroupSelected = true;
        } else {
            newAppGroupSelected = false;
        }
    }

    private void reloadAppGroups() {
        this.appGroups = stream(appGroupManager.getAllAppGroups())
                .sorted()
                .collect(Collectors.toList());
    }

    public List<String> getAppGroups() {
        return this.appGroups;
    }

    public void setAppGroups(List<String> appGroups) {
        this.appGroups = appGroups;

        notifyChange();
    }

    public boolean hasAppGroups() {
        return !appGroups.isEmpty();
    }

    public String getNewAppGroupName() {
        return this.newAppGroupName;
    }

    public void setNewAppGroupName(String newAppGroupName) {
        this.newAppGroupName = newAppGroupName;

        // if the new app group name is updated, we assume that the user is choosing to create a new app group
        setNewAppGroupSelected(true);
        notifyChange();
    }

    @Bindable
    public boolean isNewAppGroupSelected() {
        return this.newAppGroupSelected;
    }

    public void setNewAppGroupSelected(boolean newAppGroupSelected) {
        this.newAppGroupSelected = newAppGroupSelected;

        notifyChange();
    }

    public int getSelectedAppGroupPosition() {
        return this.selectedAppGroupPosition;
    }

    public void setSelectedAppGroupPosition(int selectedAppGroupPosition) {
        this.selectedAppGroupPosition = selectedAppGroupPosition;

        // if the position is changed, we assume that the user is choosing to use one of the created app groups
        setNewAppGroupSelected(false);

        notifyChange();
    }

    /**
     * Primary method to get packages added to the app groups.
     * Once this method is called, we will also update the dialog states.
     */
    public void addPackageNamesToAppGroup() {
        Set<ApplicationModel> applicationModels = stream((List<ApplicationModel>)dataSource.getRxAdapter().getDataSet())
                .filter(applicationModel -> applicationModel.isSelected())
                .collect(Collectors.toSet());

        Set<String> packageNames = stream(applicationModels)
                .map(applicationModel -> applicationModel.getPackageName())
                .collect(Collectors.toSet());

        String selectedAppGroup = newAppGroupSelected ? newAppGroupName : appGroups.get(selectedAppGroupPosition);

        appGroupManager.addPackagesToAppGroup(packageNames, selectedAppGroup);

        applicationModels.forEach(
                applicationModel ->
                        applicationModel.setSelected(false));

        // reset
        setNewAppGroupName("");
        reloadAppGroups();

        int selectedAppGroupIndex = appGroups.indexOf(selectedAppGroup);
        setSelectedAppGroupPosition(selectedAppGroupIndex >= 0 ? selectedAppGroupIndex : 0);
        setNewAppGroupSelected(false);
    }

    public void onUseCreatedAppGroupsSelected() {
        setNewAppGroupSelected(false);
    }

    public void onNewAppGroupSelected() {
        setNewAppGroupSelected(true);
    }
}
