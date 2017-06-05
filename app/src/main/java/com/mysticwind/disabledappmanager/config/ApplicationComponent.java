package com.mysticwind.disabledappmanager.config;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.app.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.backup.AppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigService;
import com.mysticwind.disabledappmanager.domain.config.BackupConfig_;
import com.mysticwind.disabledappmanager.domain.config.view.ViewOptionConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.ui.widget.config.WidgetConfigDataAccessor;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = { ApplicationModule.class })
public interface ApplicationComponent {
    void inject(Context context);
    void inject(AutoDisablingConfig_ autoDisablingConfig);
    void inject(BackupConfig_ backupConfig);
    PackageListProvider packageListProvider();
    PackageAssetService packageAssetService();
    AppAssetUpdateEventManager appAssetUpdateEventManager();
    AppStateProvider appStateProvider();
    PackageStateUpdateEventManager packageStateUpdateEventManager();
    PackageStateController packageStateController();
    AppLauncher appLauncher();
    DisabledPackageStateDecider disabledPackageStateDecider();
    ManualStateUpdateEventManager manualStateUpdateEventManager();
    AppGroupManager appGroupManager();
    AppGroupBackupManager appGroupBackupManager();
    AppGroupUpdateEventManager appGroupUpdateEventManager();

    Drawable defaultIconStubDrawable();

    WidgetConfigDataAccessor widgetConfigDataAccessor();

    ViewOptionConfigDataAccessor viewOptionConfigDataAccessor();
    AutoDisablingConfigService autoDisablingConfigService();
    BackupConfigService backupConfigService();
}
