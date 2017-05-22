package com.mysticwind.disabledappmanager.domain.backup;

import android.os.Environment;

import java.io.File;

public final class Constants {
    public static final File LEGACY_DEFAULT_DOWNLOAD_DIRECTORY =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
    public static final String APPFRIDGE_DIRECTORY_NAME = "AppFridge";
    public static final String BACKUP_DIRECTORY_NAME = "Backup";
    public static final String APPFRIDGE_BACKUP_PATH_NAME = APPFRIDGE_DIRECTORY_NAME + "/" + BACKUP_DIRECTORY_NAME;
    public static final File BACKUP_DIRECTORY = new File(LEGACY_DEFAULT_DOWNLOAD_DIRECTORY, APPFRIDGE_BACKUP_PATH_NAME);
    public static final String BACKUP_FILE_PREFIX = "appgroup-";
    public static final String BACKUP_FILE_TYPE_SUFFIX = ".json";
}
