package com.mysticwind.disabledappmanager;

import android.app.Application;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.mysticwind.disabledappmanager.config.ApplicationComponent;
import com.mysticwind.disabledappmanager.config.ApplicationModule;
import com.mysticwind.disabledappmanager.config.DaggerApplicationComponent;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.app.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.backup.AppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigService;
import com.mysticwind.disabledappmanager.domain.config.BackupConfig_;
import com.mysticwind.disabledappmanager.domain.config.application.AppStateConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.application.impl.AppStateConfig_;
import com.mysticwind.disabledappmanager.domain.config.view.ViewOptionConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.view.impl.ViewOptionConfig_;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.ui.widget.config.WidgetConfigDataAccessor;

import net.danlew.android.joda.JodaTimeAndroid;

import org.androidannotations.annotations.EApplication;
import org.androidannotations.annotations.sharedpreferences.Pref;

@EApplication
public class AppFridgeApplication extends Application implements ApplicationComponent {

    private ApplicationComponent component;

    @Pref
    ViewOptionConfig_ viewOptionConfig;

    @Pref
    AutoDisablingConfig_ autoDisablingConfig;

    @Pref
    BackupConfig_ backupConfig;

    @Pref
    AppStateConfig_ appStateConfig;

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this,
                        viewOptionConfig, autoDisablingConfig, backupConfig, appStateConfig))
                .build();
    }

    @Override
    public void inject(Context context) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public void inject(AutoDisablingConfig_ autoDisablingConfig) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public void inject(BackupConfig_ backupConfig) {
        throw new RuntimeException("Unsupported operation");
    }

    @Override
    public PackageListProvider packageListProvider() {
        return component.packageListProvider();
    }

    @Override
    public PackageAssetService packageAssetService() {
        return component.packageAssetService();
    }

    @Override
    public AppAssetUpdateEventManager appAssetUpdateEventManager() {
        return component.appAssetUpdateEventManager();
    }

    @Override
    public AppStateProvider appStateProvider() {
        return component.appStateProvider();
    }

    @Override
    public PackageStateUpdateEventManager packageStateUpdateEventManager() {
        return component.packageStateUpdateEventManager();
    }

    @Override
    public PackageStateController packageStateController() {
        return component.packageStateController();
    }

    @Override
    public AppLauncher appLauncher() {
        return component.appLauncher();
    }

    @Override
    public DisabledPackageStateDecider disabledPackageStateDecider() {
        return component.disabledPackageStateDecider();
    }

    @Override
    public AutoDisablingConfigService autoDisablingConfigService() {
        return component.autoDisablingConfigService();
    }

    @Override
    public BackupConfigService backupConfigService() {
        return component.backupConfigService();
    }

    @Override
    public AppStateConfigDataAccessor appStateConfigDataAccessor() {
        return component.appStateConfigDataAccessor();
    }

    @Override
    public ManualStateUpdateEventManager manualStateUpdateEventManager() {
        return component.manualStateUpdateEventManager();
    }

    @Override
    public AppGroupManager appGroupManager() {
        return component.appGroupManager();
    }

    @Override
    public AppGroupBackupManager appGroupBackupManager() {
        return component.appGroupBackupManager();
    }

    @Override
    public AppGroupUpdateEventManager appGroupUpdateEventManager() {
        return component.appGroupUpdateEventManager();
    }

    @Override
    public Drawable defaultIconStubDrawable() {
        return component.defaultIconStubDrawable();
    }

    @Override
    public WidgetConfigDataAccessor widgetConfigDataAccessor() {
        return component.widgetConfigDataAccessor();
    }

    @Override
    public ViewOptionConfigDataAccessor viewOptionConfigDataAccessor() {
        return component.viewOptionConfigDataAccessor();
    }
}
