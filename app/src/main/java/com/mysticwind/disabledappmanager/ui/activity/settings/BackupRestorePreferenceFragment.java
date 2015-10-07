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
        backupAppGroupsNowPreference.setSummary(
                "Backup will be generated in " + Constants.BACKUP_DIRECTORY.getAbsolutePath());
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

                        if (success) {
                            Toast.makeText(getActivity(), "Backup success!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Backup failure!", Toast.LENGTH_SHORT).show();
                        }
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
        restoreAppGroupsPreference.setSummary("Restore a backup from " + backupDirectory.getAbsolutePath());

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
                new AsyncTask<Void, Void, Void>() {
                    ProgressDialog progressDialog;
                    @Override
                    protected void onPreExecute() {
                        super.onPreExecute();
                        progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setTitle("Restoring");
                        progressDialog.setIndeterminate(true);
                        progressDialog.setCancelable(false);
                        progressDialog.create();
                        progressDialog.show();
                        restoreAppGroupsPreference.setSelectable(false);
                    }

                    @Override
                    protected Void doInBackground(Void... params) {
                        appGroupBackupManager.restore(backupUniqueId);
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        progressDialog.dismiss();
                        restoreAppGroupsPreference.setSelectable(true);
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
