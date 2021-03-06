package com.mysticwind.disabledappmanager.domain;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.domain.config.AutoDisablingConfigService;
import com.mysticwind.disabledappmanager.domain.state.DisabledPackageStateDecider;
import com.mysticwind.disabledappmanager.domain.state.DisabledStateDetectionRequest;
import com.mysticwind.disabledappmanager.ui.common.DialogHelper;

import java.util.Arrays;

public class AutoDisablingAppLauncher implements AppLauncher {
    private final AutoDisablingConfigService autoDisablingConfigService;
    private final PackageManager packageManager;
    private final AppStateProvider appStateProvider;
    private final PackageStateController packageStateController;
    private final DisabledPackageStateDecider disabledPackageStateDecider;

    public AutoDisablingAppLauncher(AutoDisablingConfigService autoDisablingConfigService,
                                    PackageManager packageManager,
                                    AppStateProvider appStateProvider,
                                    PackageStateController packageStateController,
                                    DisabledPackageStateDecider disabledPackageStateDecider) {
        this.autoDisablingConfigService = autoDisablingConfigService;
        this.packageManager = packageManager;
        this.appStateProvider = appStateProvider;
        this.packageStateController = packageStateController;
        this.disabledPackageStateDecider = disabledPackageStateDecider;
    }

    @Override
    public void launch(Context context, String packageName) {
        Dialog progressDialog = DialogHelper.newProgressDialog(context);
        new AppLauncherAsyncTask(
                context, progressDialog, packageName,
                autoDisablingConfigService.isAutoDisablingOn(),
                autoDisablingConfigService.getAutoDisablingTimeoutInSeconds())
                .execute();
    }

    private enum AppLaunchProgress {
        NOTIFY_APP_ENABLING,
        APP_LAUNCH_READY,
        NOTIFY_NO_LAUNCHING_INTENT,
        NOTIFY_APP_DISABLED,
        LAUNCH_APPLICATION,
    }

    private class AppLauncherAsyncTask extends AsyncTask<Void, AppLaunchProgress, Void> {
        private final Context context;
        private final Dialog progressDialog;
        private final String packageName;
        private Intent applicationLaunchIntent;
        private boolean isAutoDisablingOn;
        private long inactiveTimeoutInSeconds;
        private boolean packageEnabledBeforeLaunching = false;

        AppLauncherAsyncTask(Context context,
                             Dialog progressDialog,
                             String packageName,
                             boolean isAutoDisablingOn,
                             long inactiveTimeoutInSeconds) {
            this.context = context;
            this.progressDialog = progressDialog;
            this.packageName = packageName;
            this.isAutoDisablingOn = isAutoDisablingOn;
            this.inactiveTimeoutInSeconds = inactiveTimeoutInSeconds;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            boolean isEnabled = appStateProvider.isPackageEnabled(packageName);
            if (!isEnabled) {
                publishProgress(AppLaunchProgress.NOTIFY_APP_ENABLING);
                packageStateController.enablePackages(Arrays.asList(packageName));
                this.packageEnabledBeforeLaunching = true;
            }
            applicationLaunchIntent = packageManager.getLaunchIntentForPackage(packageName);
            publishProgress(AppLaunchProgress.APP_LAUNCH_READY);
            if (applicationLaunchIntent == null) {
                publishProgress(AppLaunchProgress.NOTIFY_NO_LAUNCHING_INTENT);
                /* disable back if the original status is disabled */
                if (!isEnabled) {
                    packageStateController.disablePackages(Arrays.asList(packageName));
                    publishProgress(AppLaunchProgress.NOTIFY_APP_DISABLED);
                }
            } else {
                publishProgress(AppLaunchProgress.LAUNCH_APPLICATION);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(AppLaunchProgress... values) {
            super.onProgressUpdate(values);
            if (values.length == 0) {
                return;
            }
            AppLaunchProgress progress = values[0];
            switch (progress) {
                case NOTIFY_APP_ENABLING:
                    Toast.makeText(
                            context, context.getResources().getString(
                                    R.string.toast_enabled_packages_msg_prefix) + " " + packageName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case APP_LAUNCH_READY:
                    progressDialog.dismiss();
                    break;
                case NOTIFY_NO_LAUNCHING_INTENT:
                    Toast.makeText(context, context.getResources().getString(
                                    R.string.toast_warn_no_launcher_intent_msg_prefix) + " " + packageName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case NOTIFY_APP_DISABLED:
                    Toast.makeText(context, context.getResources().getString(
                                    R.string.toast_disabled_packages_msg_prefix) + " " + packageName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case LAUNCH_APPLICATION:
                    context.startActivity(applicationLaunchIntent);
                    if (isAutoDisablingOn && packageEnabledBeforeLaunching) {
                        disabledPackageStateDecider.addDetectionRequest(
                                new DisabledStateDetectionRequest(packageName, inactiveTimeoutInSeconds));
                    }
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }
}
