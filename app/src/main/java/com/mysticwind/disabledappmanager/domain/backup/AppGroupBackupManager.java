package com.mysticwind.disabledappmanager.domain.backup;

import android.net.Uri;

import java.util.List;

public interface AppGroupBackupManager {
    void executeBackup();
    List<BackupIdentifier> getOrderedBackups();
    void restore(String backupUniqueId);
    String getHumanReadableBackupPath();
    void setBackupDirectory(Uri backupDirectoryUri);
    boolean canUpdateBackupPath();
}
