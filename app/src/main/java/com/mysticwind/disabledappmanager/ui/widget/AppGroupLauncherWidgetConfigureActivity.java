package com.mysticwind.disabledappmanager.ui.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.ui.widget.config.WidgetConfigDataAccessor;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

/**
 * The configuration screen for the {@link AppGroupLauncherWidget AppGroupLauncherWidget} AppWidget.
 */
@Slf4j
@EActivity
public class AppGroupLauncherWidgetConfigureActivity extends Activity {
    private AppGroupManager appGroupManager;
    private WidgetConfigDataAccessor widgetConfigDataAccessor;
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @ViewById(R.id.app_group_spinner)
    Spinner appGroupSpinner;

    private View.OnClickListener onButtonClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            final Context context = AppGroupLauncherWidgetConfigureActivity.this;

            // nothing has been selected
            final View view = appGroupSpinner.getSelectedView();
            if (view == null) {
                return;
            }
            CharSequence text = ((TextView) appGroupSpinner.getSelectedView()).getText();
            if (text == null || text.length() == 0) {
                return;
            }
            // When the button is clicked, store the string locally
            String appGroupName = text.toString();
            saveAppGroupNameForWidget(appGroupName, appWidgetId);

            // It is the responsibility of the configuration activity to update the app widget
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            AppGroupLauncherWidget.updateAppWidget(context, appWidgetManager, appWidgetId);

            // Make sure we pass back the original appWidgetId
            Intent resultValue = new Intent();
            resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            setResult(RESULT_OK, resultValue);
            finish();
        }
    };

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        appGroupManager = ApplicationHelper.from(this).appGroupManager();
        widgetConfigDataAccessor = ApplicationHelper.from(this).widgetConfigDataAccessor();

        // Set the result to CANCELED.  This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.app_group_launcher_widget_configure);
        final Button addWidgetButton = (Button) findViewById(R.id.add_button);
        addWidgetButton.setOnClickListener(onButtonClickListener);

        // Find the widget id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            appWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            log.warn("Invalid widget ID: " + appWidgetId);
            finish();
            return;
        }

        List<String> appGroupList = new ArrayList<>(appGroupManager.getAllAppGroups());

        appGroupSpinner.setAdapter(new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_dropdown_item, appGroupList));
        // the add widget button is disabled by default and will be enabled if an item is being selected
        appGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                addWidgetButton.setEnabled(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                addWidgetButton.setEnabled(false);
            }
        });
    }

    // Write the prefix to the SharedPreferences object for this widget
    private void saveAppGroupNameForWidget(final String appGroupName, final int appWidgetId) {
        widgetConfigDataAccessor.addWidget(appWidgetId, appGroupName);
    }
}

