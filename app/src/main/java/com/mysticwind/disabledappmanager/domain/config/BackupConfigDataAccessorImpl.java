package com.mysticwind.disabledappmanager.domain.config;

public class BackupConfigDataAccessorImpl implements BackupConfigDataAccessor {

    private final BackupConfig_ config;

    public BackupConfigDataAccessorImpl(BackupConfig_ config) {
        this.config = config;
    }

    @Override
    public String getBackupPath() {
        return config.backupPath().get();
    }

    @Override
    public void setBackupPath(String backupPath) {
        config.edit().backupPath().put(backupPath).apply();
    }
}
