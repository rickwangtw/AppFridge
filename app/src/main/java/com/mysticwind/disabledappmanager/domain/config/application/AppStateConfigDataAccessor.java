package com.mysticwind.disabledappmanager.domain.config.application;

public interface AppStateConfigDataAccessor {

    boolean shouldShowPackageStatePerspectiveTutorial();
    void updatePackageStatePerspectiveTutorialShown();

    boolean shouldShowAppGroupPerspectiveTutorial();
    void updateAppGroupPerspectiveTutorialShown();

}
