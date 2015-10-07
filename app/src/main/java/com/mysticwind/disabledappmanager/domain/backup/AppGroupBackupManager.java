package com.mysticwind.disabledappmanager.domain.backup;

import java.util.List;

public interface AppGroupBackupManager {
    void backup();
    List<BackupIdentifier> getAllBackupsOrdered();
    void restore(String backupUniqueId);
}
