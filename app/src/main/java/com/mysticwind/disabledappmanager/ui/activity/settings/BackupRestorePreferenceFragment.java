package com.mysticwind.disabledappmanager.ui.activity.settings;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.mysticwind.disabledappmanager.R;
import com.mysticwind.disabledappmanager.common.ApplicationHelper;
import com.mysticwind.disabledappmanager.domain.backup.AppGroupBackupManager;
import com.mysticwind.disabledappmanager.domain.backup.BackupIdentifier;
import com.mysticwind.disabledappmanager.domain.backup.Constants;

import org.androidannotations.annotations.AfterPreferences;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.PreferenceByKey;
import org.androidannotations.annotations.PreferenceScreen;
import org.joda.time.DateTime;

import java.io.File;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@PreferenceScreen(R.xml.pref_backup)
@EFragment
@Slf4j
public class BackupRestorePreferenceFragment extends PreferenceFragment {

    private AppGroupBackupManager appGroupBackupManager;

    @PreferenceByKey(R.string.pref_key_backup_appgroups_now)
    SwitchPreference backupAppGroupsNowPreference;

    @PreferenceByKey(R.string.pref_key_restore_appgroups_now)
    ListPreference restoreAppGroupsPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appGroupBackupManager = ApplicationHelper.from(this).appGroupBackupManager();
    }

    @AfterPreferences
    void configurePreferences() {
        setupBackupAppGroupsNowPreference();
        setupRestoreAppGroupsNowPreference();
    }

    private void setupBackupAppGroupsNowPreference() {
        String descriptionPrefix =
                getString(R.string.settings_backup_preference_app_group_backup_path_description);
        backupAppGroupsNowPreference.setSummary(
                descriptionPrefix + " " + Constants.BACKUP_DIRECTORY.getAbsolutePath());
        backupAppGroupsNowPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                final boolean enable = (Boolean) newValue;
                if (!enable) {
                    return true;
                }
                backupAppGroupsNowPreference.setSelectable(false);
                new AsyncTask<Void, Void, Boolean>() {
                    private Throwable throwable;

                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            appGroupBackupManager.backup();
                            return true;
                        } catch (Throwable t) {
                            this.throwable = t;
                            log.error("Failed when doing backup!", t);
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        super.onPostExecute(success);

                        int toastStringResourceId;
                        if (success) {
                            toastStringResourceId = R.string.toast_backup_success;
                        } else {
                            toastStringResourceId = R.string.toast_backup_failure;
                        }
                        Toast.makeText(getActivity(), toastStringResourceId, Toast.LENGTH_SHORT).show();

                        backupAppGroupsNowPreference.setChecked(false);
                        backupAppGroupsNowPreference.setSelectable(true);
                        // update the groups available to restore
                        setupRestoreAppGroupsNowPreference();
                    }
                }.execute();
                return true;
            }
        });
    }

    private void setupRestoreAppGroupsNowPreference() {
        File backupDirectory = Constants.BACKUP_DIRECTORY;
        String descriptionPrefix =
                getString(R.string.settings_backup_preference_app_group_restore_path_description);
        restoreAppGroupsPreference.setSummary(descriptionPrefix + " " + backupDirectory.getAbsolutePath());

        List<BackupIdentifier> backupList = appGroupBackupManager.getAllBackupsOrdered();

        CharSequence[] backupDateTimeLabels = new CharSequence[backupList.size()];
        CharSequence[] backupUniqueIds = new CharSequence[backupList.size()];

        for (int backupFileIndex = 0 ; backupFileIndex < backupList.size(); ++backupFileIndex) {
            BackupIdentifier backupIdentifier = backupList.get(backupFileIndex);
            backupDateTimeLabels[backupFileIndex] = getLabelFrom(backupIdentifier.getCreatedDateTime());
            backupUniqueIds[backupFileIndex] = backupIdentifier.getUniqueId();
        }

        restoreAppGroupsPreference.setEntries(backupDateTimeLabels);
        restoreAppGroupsPreference.setEntryValues(backupUniqueIds);
        restoreAppGroupsPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                // always return false as we don't need to save the preference
                final String backupUniqueId = (String) newValue;
                new AsyncTask<Void, Void, Boolean>() {
                    private ProgressDialog progressDialog;
                    private Throwable throwable;

                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle(R.string.settings_backup_preference_app_group_restore_dialog_title);
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        restoreAppGroupsPreference.setSelectable(false);
                    }

                    @Override
                    protected Boolean doInBackground(Void... params) {
                        try {
                            appGroupBackupManager.restore(backupUniqueId);
                            return true;
                        } catch (Throwable t) {
                            throwable = t;
                            log.error("Failed to restore app groups!", t);
                            return false;
                        }
                    }

                    @Override
                    protected void onPostExecute(Boolean success) {
                        super.onPostExecute(success);
                        progressDialog.dismiss();
                        restoreAppGroupsPreference.setSelectable(true);

                        int toastStringResourceId;
                        if (success) {
                            toastStringResourceId = R.string.toast_restore_success;
                        } else {
                            toastStringResourceId = R.string.toast_restore_failure;
                        }
                        Toast.makeText(getActivity(), toastStringResourceId, Toast.LENGTH_SHORT).show();
                    }
                }.execute();
                return false;
            }
        });
    }

    private String getLabelFrom(DateTime dateTime) {
        String dateTimeLabel = DateUtils.formatDateTime(getActivity(), dateTime.getMillis(),
                DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        return dateTimeLabel;
    }
}
