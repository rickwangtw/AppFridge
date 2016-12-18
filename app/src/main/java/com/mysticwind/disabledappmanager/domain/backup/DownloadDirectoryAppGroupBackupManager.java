package com.mysticwind.disabledappmanager.domain.backup;

import android.content.ContentResolver;
import android.support.v4.provider.DocumentFile;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadDirectoryAppGroupBackupManager implements AppGroupBackupManager {
    private static final String BACKUP_FILE_FORMAT =
            Constants.BACKUP_FILE_PREFIX + "%s-%d" + Constants.BACKUP_FILE_TYPE_SUFFIX;
    // sample file name appgroup-2015-10-07-1444160807513.json
    private static final Pattern BACKUP_FILE_NAME_PATTERN = Pattern.compile(
            String.format("^%s\\d{4}-\\d{2}-\\d{2}-(\\d+)%s$", Constants.BACKUP_FILE_PREFIX, Constants.BACKUP_FILE_TYPE_SUFFIX));
    private static final Charset defaultCharset = Charsets.UTF_8;

    private final AppGroupManager appGroupManager;
    private final AppGroupUpdateEventManager appGroupUpdateEventManager;
    private final ContentResolver contentResolver;

    public DownloadDirectoryAppGroupBackupManager(final AppGroupManager appGroupManager,
                                                  final AppGroupUpdateEventManager appGroupUpdateEventManager,
                                                  final ContentResolver contentResolver) {
        this.appGroupManager = appGroupManager;
        this.appGroupUpdateEventManager = appGroupUpdateEventManager;
        this.contentResolver = contentResolver;
    }

    @Override
    public void backup(DocumentFile backupDirectory) {
        Map<String, Set<String>> appGroupToPackagesMap = new HashMap<>();
        Set<String> appGroups = appGroupManager.getAllAppGroups();

        for (String appGroup : appGroups) {
            Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroup);
            appGroupToPackagesMap.put(appGroup, packages);
        }
        Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
        String appGroupJson = new Gson().toJson(appGroupToPackagesMap, type);

        if (!backupDirectory.canWrite()) {
            throw new IllegalStateException("Unable to write in directory: " + backupDirectory.getUri());
        }

        DocumentFile backupFile = backupDirectory.createFile("application/javascript", getBackupFileName());
        OutputStream outputStream;
        try {
            outputStream = contentResolver.openOutputStream(backupFile.getUri());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + backupFile.getUri());
        }

        try {
            outputStream.write(appGroupJson.getBytes(defaultCharset));
        } catch (IOException e) {
            throw new RuntimeException("Failed to write file: " + backupFile.getUri());
        } finally {
            try {
                Closeables.close(outputStream, false);
            } catch (IOException e) {
            }
        }
    }

    @Override
    public List<BackupIdentifier> getBackupsOrderedUnderDirectory(DocumentFile backupDirectory) {
        TreeSet<BackupIdentifier> backupIdentifierTreeSet = new TreeSet<>(new Comparator<BackupIdentifier>() {
            @Override
            public int compare(BackupIdentifier backupIdentifier1, BackupIdentifier backupIdentifier2) {
                return -1 * backupIdentifier1.getCreatedDateTime().compareTo(backupIdentifier2.getCreatedDateTime());
            }
        });

        for (DocumentFile file : backupDirectory.listFiles()) {
            if (file.isDirectory()) {
                continue;
            }
            String fileName = file.getName();
            Matcher backupFileNameMatcher = BACKUP_FILE_NAME_PATTERN.matcher(fileName);
            if (!backupFileNameMatcher.matches()) {
                continue;
            }
            long timestamp = Long.parseLong(backupFileNameMatcher.group(1));
            DateTime createdDateTime = new DateTime(timestamp);

            backupIdentifierTreeSet.add(new BackupIdentifier(file.getName(), createdDateTime));
        }
        return ImmutableList.copyOf(backupIdentifierTreeSet);
    }

    private File getBackupFile() {
        File backupFile = new File(Constants.BACKUP_DIRECTORY, getBackupFileName());
        if (backupFile.exists()) {
            throw new RuntimeException("Backup file exists: " + backupFile.getAbsolutePath());
        }
        try {
            Files.createParentDirs(backupFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create parent directories for " + backupFile);
        }
        return backupFile;
    }

    private String getBackupFileName() {
        return String.format(BACKUP_FILE_FORMAT, new LocalDate().toString(), System.currentTimeMillis());
    }

    @Override
    public void restore(DocumentFile backupDirectory, String backupUniqueId) {
        DocumentFile fileToRestore = backupDirectory.findFile(backupUniqueId);
        if (!fileToRestore.canRead()) {
            throw new IllegalStateException("Unable to read file to restore: " + backupUniqueId);
        }
        InputStream inputStream = null;
        try {
            inputStream = contentResolver.openInputStream(fileToRestore.getUri());
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found: " + backupUniqueId, e);
        }
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream);

        Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
        Map<String, Set<String>> appGroupToPackagesMap = new Gson().fromJson(inputStreamReader, type);
        restoreWithEventPublished(appGroupToPackagesMap);

        Closeables.closeQuietly(inputStreamReader);
    }

    private void restoreWithEventPublished(Map<String, Set<String>> appGroupToPackagesMap) {
        for (Map.Entry<String, Set<String>> entry : appGroupToPackagesMap.entrySet()) {
            String appGroupName = entry.getKey();
            Set<String> packages = entry.getValue();

            Set<String> persistedPackages = appGroupManager.getPackagesOfAppGroup(appGroupName);
            packages.remove(persistedPackages);

            if (!packages.isEmpty()) {
                appGroupManager.addPackagesToAppGroup(packages, appGroupName);
            }
        }
    }
}
