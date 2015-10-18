package com.mysticwind.disabledappmanager.ui.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.ui.widget.config.WidgetConfigDataAccessor;

import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of App Widget functionality.
 * App Widget Configuration implemented in {@link AppGroupLauncherWidgetConfigureActivity AppGroupLauncherWidgetConfigureActivity}
 */
@Slf4j
public class AppGroupLauncherWidget extends AppWidgetProvider {
    private static final String LAUNCH_APP_LIST_INTENT_ACTION = "LAUNCH_APP_LIST";

    private static WidgetConfigDataAccessor widgetConfigDataAccessor;

    private static Boolean initialized = false;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        initializeWidget(context);

        // There may be multiple widgets active, so update all of them
        for (int appWidgetIdIndex = 0; appWidgetIdIndex < appWidgetIds.length; appWidgetIdIndex++) {
            updateAppWidget(context, appWidgetManager, appWidgetIds[appWidgetIdIndex]);
        }
    }

    private static void initializeWidget(Context context) {
        synchronized (initialized) {
            if (!initialized) {
                widgetConfigDataAccessor = ApplicationHelper.from(context).widgetConfigDataAccessor();
                initialized = true;
            }
        }
    }

    private static Intent buildLaunchAppGroupDialogIntent(Context context, String appGroupName) {
        Intent appGroupDialogIntent = new Intent(context, AppGroupPackageGridDialogActivity_.class);
        appGroupDialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appGroupDialogIntent.putExtra(WidgetConstants.APP_GROUP_NAME_EXTRA_KEY, appGroupName);
        return appGroupDialogIntent;
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        // When the user deletes the widget, delete the preference associated with it.
        for (int appWidgetIdIndex = 0; appWidgetIdIndex < appWidgetIds.length; appWidgetIdIndex++) {
            int widgetId = appWidgetIds[appWidgetIdIndex];
            widgetConfigDataAccessor.removeWidget(widgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        initializeWidget(context);

        String appGroupName;
        try {
            appGroupName = widgetConfigDataAccessor.getWidgetAppGroup(appWidgetId);
        } catch (Exception e) {
            log.warn("Unable to find app group name for " + appWidgetId, e);
            appGroupName = WidgetConstants.UNINITIALIZED_APP_GROUP_NAME;
        }

        RemoteViews views = constructWidgetRemoteView(context, appGroupName);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    private static RemoteViews constructWidgetRemoteView(Context context, String appGroupName) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.app_group_launcher_widget);
        views.setTextViewText(R.id.appwidget_text, appGroupName);

        if (!WidgetConstants.UNINITIALIZED_APP_GROUP_NAME.equals(appGroupName)) {
            Intent appGroupDialogIntent = buildLaunchAppGroupDialogIntent(context, appGroupName);
            PendingIntent launchAppListEvent = PendingIntent.getActivity(
                    context, appGroupName.hashCode(), appGroupDialogIntent, 0);
            views.setOnClickPendingIntent(R.id.appgroup_widget_layout, launchAppListEvent);
        }
        return views;
    }
}

