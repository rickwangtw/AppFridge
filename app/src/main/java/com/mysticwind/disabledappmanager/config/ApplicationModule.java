package com.mysticwind.disabledappmanager.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

import com.gmr.acacia.Acacia;
import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.AppGroupManagerImpl;
import com.mysticwind.disabledappmanager.domain.AppIconProvider;
import com.mysticwind.disabledappmanager.domain.AppLauncher;
import com.mysticwind.disabledappmanager.domain.AppNameProvider;
import com.mysticwind.disabledappmanager.domain.AppStateProvider;
import com.mysticwind.disabledappmanager.domain.asset.PackageAssetService;
import com.mysticwind.disabledappmanager.domain.AutoDisablingAppLauncher;
import com.mysticwind.disabledappmanager.domain.PackageMangerAppStateProvider;
import com.mysticwind.disabledappmanager.domain.PackageStateController;
import com.mysticwind.disabledappmanager.domain.RootProcessPackageStateController;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.appgroup.EventBusAppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.AppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.EventBusAppAssetUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.asset.PackageManagerPackageAssetService;
import com.mysticwind.disabledappmanager.domain.backup.AppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.backup.DownloadDirectoryAppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.config.AnnotationGeneratedConfigAutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessor;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigDataAccessorImpl;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfig_;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.EventBusManualStateUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.state.ManualStateUpdateEventManager;
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
    private final Context context;
    private final AutoDisablingConfig_ autoDisablingConfig;

    public ApplicationModule(Context context, AutoDisablingConfig_ autoDisablingConfig) {
        this.context = context;
        this.autoDisablingConfig = autoDisablingConfig;
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
    public PackageManager providePackageManager() {
        return this.context.getPackageManager();
    }

    @Provides @Singleton
    public Drawable provideDefaultIconStubDrawable() {
        return context.getResources().getDrawable(R.drawable.stub);
    }

    @Provides @Singleton
    public PackageAssetService providePackageAssetService(
            PackageManager packageManager, AppAssetUpdateEventManager appAssetUpdateEventManager,
            Drawable defaultIcon) {
        return new PackageManagerPackageAssetService(
                packageManager, appAssetUpdateEventManager, defaultIcon);
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
    public AppAssetUpdateEventManager provideAppAssetUpdateEventManager(EventBus eventBus) {
        return new EventBusAppAssetUpdateEventManager(eventBus);
    }

    @Provides @Singleton
    public AppStateProvider provideAppStateProvider(PackageManager packageManager) {
        return new PackageMangerAppStateProvider(packageManager);
    }

    @Provides @Singleton
    public PackageStateController providePackageStateController() {
        return new RootProcessPackageStateController();
    }

    @Provides @Singleton
    public AutoDisablingConfigService provideAutoDisablingConfigService(
            AutoDisablingConfigDataAccessor autoDisablingConfigDataAccessor) {
        return new AnnotationGeneratedConfigAutoDisablingConfigService(autoDisablingConfigDataAccessor);
    }

    @Provides @Singleton
    public AppLauncher provideAppLauncher(
            AutoDisablingConfigService autoDisablingConfigService,
            PackageManager packageManager,
            AppStateProvider appStateProvider,
            PackageStateController packageStateController) {
        return new AutoDisablingAppLauncher(autoDisablingConfigService,
                packageManager, appStateProvider, packageStateController);
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
    public AutoDisablingConfigDataAccessor provideAutoDisablingConfigDataAccessor(
            AutoDisablingConfig_ autoDisablingConfig) {
        return new AutoDisablingConfigDataAccessorImpl(autoDisablingConfig);
    }

    @Provides @Singleton
    public EventBus provideEventBus() {
        return EventBus.getDefault();
    }

    @Provides @Singleton
    public ManualStateUpdateEventManager provideManualStateUpdateEventManager(EventBus eventBus) {
        return new EventBusManualStateUpdateEventManager(eventBus);
    }

    @Provides @Singleton
    public AppGroupManager provideAppGroupManager(
            Context context, AppGroupUpdateEventManager appGroupUpdateEventManager) {
        return new AppGroupManagerImpl(new AppGroupDAO(context), appGroupUpdateEventManager);
    }

    @Provides @Singleton
    public AppGroupUpdateEventManager provideAppGroupUpdateEventManager(EventBus eventBus) {
        return new EventBusAppGroupUpdateEventManager(eventBus);
    }

    @Provides @Singleton
    public AppGroupBackupManager provideAppGroupBackupManager(
                    AppGroupManager appGroupManager,
                    AppGroupUpdateEventManager appGroupUpdateEventManager ) {
        return new DownloadDirectoryAppGroupBackupManager(appGroupManager, appGroupUpdateEventManager);
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
