package com.mysticwind.disabledappmanager.ui.widget.config;

public interface WidgetConfigDataAccessor {
    void addWidget(int widgetId, String appGroup);
    void removeWidget(int widgetId);
    String getWidgetAppGroup(int widgetId);
}
