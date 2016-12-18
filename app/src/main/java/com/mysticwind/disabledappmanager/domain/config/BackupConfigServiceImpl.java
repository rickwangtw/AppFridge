package com.mysticwind.disabledappmanager.domain.config;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BackupConfigServiceImpl implements BackupConfigService {

    private final BackupConfigDataAccessor backupConfigDataAccessor;

    public BackupConfigServiceImpl(final BackupConfigDataAccessor backupConfigDataAccessor) {
        this.backupConfigDataAccessor = backupConfigDataAccessor;
    }

    @Override
    public String getBackupPath() {
        return backupConfigDataAccessor.getBackupPath();
    }

    @Override
    public void setBackupPath(String backupPath) {
        backupConfigDataAccessor.setBackupPath(backupPath);
    }
}
