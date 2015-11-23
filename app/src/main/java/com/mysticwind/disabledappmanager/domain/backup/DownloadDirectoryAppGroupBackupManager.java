package com.mysticwind.disabledappmanager.domain.backup;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupOperation;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    public DownloadDirectoryAppGroupBackupManager(AppGroupManager appGroupManager,
                                                  AppGroupUpdateEventManager appGroupUpdateEventManager) {
        this.appGroupManager = appGroupManager;
        this.appGroupUpdateEventManager = appGroupUpdateEventManager;
    }

    @Override
    public void backup() {
        Map<String, Set<String>> appGroupToPackagesMap = new HashMap<>();
        Set<String> appGroups = appGroupManager.getAllAppGroups();

        for (String appGroup : appGroups) {
            Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroup);
            appGroupToPackagesMap.put(appGroup, packages);
        }
        String appGroupJson = new GsonBuilder().setPrettyPrinting().create().toJson(appGroupToPackagesMap);

        File backupFile = getBackupFile();
        try {
            Files.write(appGroupJson, backupFile, defaultCharset);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write app group json file to " + backupFile.getAbsolutePath(), e);
        }
    }

    @Override
    public List<BackupIdentifier> getAllBackupsOrdered() {
        if (!Constants.BACKUP_DIRECTORY.exists() || !Constants.BACKUP_DIRECTORY.canRead()) {
            return Collections.emptyList();
        }
        List<BackupIdentifier> backupIdentifierList = new LinkedList<>();
        for (File file : Files.fileTreeTraverser().preOrderTraversal(Constants.BACKUP_DIRECTORY)) {
            String fileName = file.getName();
            Matcher backupFileNameMatcher = BACKUP_FILE_NAME_PATTERN.matcher(fileName);

            if (!backupFileNameMatcher.matches()) {
                continue;
            }

            long timestamp = Long.parseLong(backupFileNameMatcher.group(1));
            DateTime createdDateTime = new DateTime(timestamp);

            backupIdentifierList.add(new BackupIdentifier(file.getName(), createdDateTime));
        }

        Collections.sort(backupIdentifierList, new Comparator<BackupIdentifier>() {
            @Override
            public int compare(BackupIdentifier lhs, BackupIdentifier rhs) {
                return reverseOrder(lhs.getCreatedDateTime().compareTo(rhs.getCreatedDateTime()));
            }
            private int reverseOrder(int comparision) {
                return comparision * -1;
            }
        });
        return backupIdentifierList;
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
    public void restore(String backupUniqueId) {
        File backupToRestore = new File(Constants.BACKUP_DIRECTORY, backupUniqueId);
        BufferedReader reader;
        try {
            reader = Files.newReader(backupToRestore, defaultCharset);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File " + backupToRestore.getAbsolutePath() + " not readable!");
        }
        Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
        Map<String, Set<String>> appGroupToPackagesMap = new Gson().fromJson(reader, type);
        restoreWithEventPublished(appGroupToPackagesMap);
        try {
            reader.close();
        } catch (IOException e) {
            log.warn("Failure closing reader for " + backupUniqueId, e);
        }
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
