package com.mysticwind.disabledappmanager.domain.backup;

import android.support.v4.provider.DocumentFile;

import java.util.List;

public interface AppGroupBackupManager {
    void backup();
    List<BackupIdentifier> getBackupsOrderedUnderDirectory(DocumentFile documentFile);
    void restore(DocumentFile backupDirectory, String backupUniqueId);
}
