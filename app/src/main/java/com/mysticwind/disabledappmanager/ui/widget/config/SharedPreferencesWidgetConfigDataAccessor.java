package com.mysticwind.disabledappmanager.ui.widget.config;

import android.content.SharedPreferences;

public class SharedPreferencesWidgetConfigDataAccessor implements WidgetConfigDataAccessor {
    private static final String WIDGET_PREFIX_KEY = "appwidget";

    private final SharedPreferences sharedPreferences;

    public SharedPreferencesWidgetConfigDataAccessor(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void addWidget(int widgetId, String appGroup) {
        sharedPreferences.edit()
                .putString(uniqueWidgetId(widgetId), appGroup)
                .commit();
    }

    private String uniqueWidgetId(int widgetId) {
        return String.format("%s-%d", WIDGET_PREFIX_KEY, widgetId);
    }

    @Override
    public void removeWidget(int widgetId) {
        sharedPreferences.edit()
                .remove(uniqueWidgetId(widgetId))
                .commit();
    }

    @Override
    public String getWidgetAppGroup(int widgetId) {
        String appGroup = sharedPreferences.getString(uniqueWidgetId(widgetId), null);
        if (appGroup == null) {
            throw new RuntimeException("Failed to get app group from widget: " + widgetId);
        }
        return appGroup;
    }
}
