package com.mysticwind.disabledappmanager.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppGroupManagerImpl;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.AutoDisablingAppLauncher;
import com.mysticwind.disabledappmanager.domain.PackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageManagerAllPackageListProvider;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.appgroup.EventBusAppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.DatabaseCachedPackageAssetServiceDecorator;
import com.mysticwind.disabledappmanager.domain.asset.DefaultValuePackageAssetServiceDecorator;
import com.mysticwind.disabledappmanager.domain.asset.EventBusAppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.MemCachedPackageAssetServiceDecorator;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.asset.PackageManagerPackageAssetService;
import com.mysticwind.disabledappmanager.domain.asset.dao.CachedPackageAssetsDAO;
import com.mysticwind.disabledappmanager.domain.backup.AppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.backup.DownloadDirectoryAppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.config.AnnotationGeneratedConfigAutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessorImpl;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigDataAccessorImpl;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigService;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigServiceImpl;
import com.mysticwind.disabledappmanager.domain.config.BackupConfig_;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.EventBusManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.EventBusPackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.PackageStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.TimerTriggeredDisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.storage.AppGroupDAO;
import com.mysticwind.disabledappmanager.domain.timer.AndroidHandlerTimerManager;
import com.mysticwind.disabledappmanager.domain.timer.TimerManager;
import com.mysticwind.disabledappmanager.ui.widget.config.SharedPreferencesWidgetConfigDataAccessor;
import com.mysticwind.disabledappmanager.ui.widget.config.WidgetConfigDataAccessor;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import de.greenrobot.event.EventBus;

@Module
public class ApplicationModule {
    private static class PackageListProviderNames {
        static final String ALL_PACKAGE_NAME = "allPackageListProvider";
    }

    private final Context context;
    private final AutoDisablingConfig_ autoDisablingConfig;
    private final BackupConfig_ backupConfig;

    public ApplicationModule(final Context context,
                             final AutoDisablingConfig_ autoDisablingConfig,
                             final BackupConfig_ backupConfig) {
        this.context = context;
        this.autoDisablingConfig = autoDisablingConfig;
        this.backupConfig = backupConfig;
    }

    @Provides @Singleton
    public Context provideContext() {
        return this.context;
    }

    @Provides @Singleton
    public AutoDisablingConfig_ provideAutoDisablingConfig() {
        return this.autoDisablingConfig;
    }

    @Provides @Singleton
    public AutoDisablingConfigDataAccessor provideAutoDisablingConfigDataAccessor(
            AutoDisablingConfig_ autoDisablingConfig) {
        return new AutoDisablingConfigDataAccessorImpl(autoDisablingConfig);
    }

    @Provides @Singleton
    public AutoDisablingConfigService provideAutoDisablingConfigService(
            AutoDisablingConfigDataAccessor autoDisablingConfigDataAccessor) {
        return new AnnotationGeneratedConfigAutoDisablingConfigService(autoDisablingConfigDataAccessor);
    }

    @Provides @Singleton
    public BackupConfig_ provideBackupConfig() {
        return this.backupConfig;
    }

    @Provides @Singleton
    public BackupConfigDataAccessor provideBackupConfigDataAccessor(BackupConfig_ backupConfig) {
        return new BackupConfigDataAccessorImpl(backupConfig);
    }

    @Provides @Singleton
    public BackupConfigService provideBackupConfigService(BackupConfigDataAccessor backupConfigDataAccessor) {
        return new BackupConfigServiceImpl(backupConfigDataAccessor);
    }

    @Provides @Singleton
    public PackageManager providePackageManager() {
        return this.context.getPackageManager();
    }

    @Provides @Singleton
    public Drawable provideDefaultIconStubDrawable() {
        return context.getResources().getDrawable(R.drawable.stub);
    }

    @Provides @Singleton
    public PackageAssetService providePackageAssetService(
            PackageManager packageManager, Context context, AppAssetUpdateEventManager appAssetUpdateEventManager,
            Drawable defaultIcon) {
        return new DefaultValuePackageAssetServiceDecorator(
                new MemCachedPackageAssetServiceDecorator(
                        new DatabaseCachedPackageAssetServiceDecorator(
                                new PackageManagerPackageAssetService(
                                        packageManager,
                                        appAssetUpdateEventManager
                                ),
                                new CachedPackageAssetsDAO(context)
                        )
                ), defaultIcon);
    }

    @Provides @Singleton
    public AppIconProvider provideAppIconProvider(PackageAssetService packageAssetService) {
        return packageAssetService;
    }

    @Provides @Singleton
    public AppNameProvider provideAppNameProvider(PackageAssetService packageAssetService) {
        return packageAssetService;
    }

    @Provides @Singleton
    public AppAssetUpdateEventManager provideAppAssetUpdateEventManager() {
        return new EventBusAppAssetUpdateEventManager(new EventBus());
    }

    @Provides @Singleton
    public AppStateProvider provideAppStateProvider(PackageManager packageManager) {
        return new PackageMangerAppStateProvider(packageManager);
    }

    @Provides @Singleton
    public PackageStateUpdateEventManager providePackageStateUpdateEventManager() {
        return new EventBusPackageStateUpdateEventManager(new EventBus());
    }

    @Provides @Singleton
    public PackageStateController providePackageStateController(
            PackageStateUpdateEventManager packageStateUpdateEventManager) {
        return new RootProcessPackageStateController(packageStateUpdateEventManager);
    }

    @Provides @Singleton
    public AppLauncher provideAppLauncher(
            AutoDisablingConfigService autoDisablingConfigService,
            PackageManager packageManager,
            AppStateProvider appStateProvider,
            PackageStateController packageStateController,
            DisabledPackageStateDecider disabledPackageStateDecider) {
        return new AutoDisablingAppLauncher(autoDisablingConfigService,
                packageManager, appStateProvider, packageStateController, disabledPackageStateDecider);
    }

    @Provides @Singleton
    public TimerManager provideTimerManager() {
        return new AndroidHandlerTimerManager();
    }

    @Provides @Singleton
    public DisabledPackageStateDecider provideDisabledPackageStateDecider(TimerManager timerManager) {
        return new TimerTriggeredDisabledPackageStateDecider(timerManager);
    }

    @Provides @Singleton
    public ManualStateUpdateEventManager provideManualStateUpdateEventManager() {
        return new EventBusManualStateUpdateEventManager(new EventBus());
    }

    @Provides @Singleton @Named(PackageListProviderNames.ALL_PACKAGE_NAME)
    public PackageListProvider providePackageListProvider(PackageManager packageManager) {
        return new PackageManagerAllPackageListProvider(packageManager);
    }

    @Provides @Singleton
    public AppGroupManager provideAppGroupManager(
            Context context,
            AppGroupUpdateEventManager appGroupUpdateEventManager,
            @Named(PackageListProviderNames.ALL_PACKAGE_NAME)
            PackageListProvider packageListProvider) {
        return new AppGroupManagerImpl(
                new AppGroupDAO(context), appGroupUpdateEventManager, packageListProvider);
    }

    @Provides @Singleton
    public AppGroupUpdateEventManager provideAppGroupUpdateEventManager() {
        return new EventBusAppGroupUpdateEventManager(new EventBus());
    }

    @Provides @Singleton
    public AppGroupBackupManager provideAppGroupBackupManager(
                    final AppGroupManager appGroupManager,
                    final AppGroupUpdateEventManager appGroupUpdateEventManager,
                    final BackupConfigService backupConfigService,
                    final Context context) {
        return new DownloadDirectoryAppGroupBackupManager(appGroupManager,
                appGroupUpdateEventManager, context.getContentResolver(), backupConfigService, context);
    }

    private static final String widgetConfigSharedPreferencesName = "widgetConfigSharedPreferencesName";

    @Provides @Singleton @Named(widgetConfigSharedPreferencesName)
    public String provideWidgetConfigSharedPreferencesName() {
        return "AppGroupWidgetConfig";
    }

    @Provides @Singleton
    public SharedPreferences provideWidgetConfigSharedPreferences(
            @Named(widgetConfigSharedPreferencesName)String sharedPreferencesName) {
        return context.getSharedPreferences(sharedPreferencesName, Context.MODE_PRIVATE);
    }

    @Provides @Singleton
    public WidgetConfigDataAccessor provideWidgetConfigDataAccessor(SharedPreferences sharedPreferences) {
        return new SharedPreferencesWidgetConfigDataAccessor(sharedPreferences);
    }
}
