package com.mysticwind.disabledappmanager.domain.backup;

import android.os.Environment;

import java.io.File;

public final class Constants {
    public static final File DOWNLOAD_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final String BACKUP_DIRECTORY_NAME = "AppFridge/Backup";
    public static final File BACKUP_DIRECTORY = new File(DOWNLOAD_DIRECTORY, BACKUP_DIRECTORY_NAME);
    public static final String BACKUP_FILE_PREFIX = "appgroup-";
    public static final String BACKUP_FILE_TYPE_SUFFIX = ".json";
}
