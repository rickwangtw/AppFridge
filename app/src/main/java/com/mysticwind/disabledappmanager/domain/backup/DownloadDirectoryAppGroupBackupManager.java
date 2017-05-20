package com.mysticwind.disabledappmanager.domain.backup;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Closeables;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mysticwind.disabledappmanager.domain.AppGroupManager;
import com.mysticwind.disabledappmanager.domain.appgroup.AppGroupUpdateEventManager;
import com.mysticwind.disabledappmanager.domain.config.BackupConfigService;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;

import static android.support.v4.provider.DocumentFile.fromTreeUri;

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
    private final BackupConfigService backupConfigService;
    private final Context context;

    public DownloadDirectoryAppGroupBackupManager(final AppGroupManager appGroupManager,
                                                  final AppGroupUpdateEventManager appGroupUpdateEventManager,
                                                  final ContentResolver contentResolver,
                                                  final BackupConfigService backupConfigService,
                                                  final Context context) {
        this.appGroupManager = appGroupManager;
        this.appGroupUpdateEventManager = appGroupUpdateEventManager;
        this.contentResolver = contentResolver;
        this.backupConfigService = backupConfigService;
        this.context = context;
    }

    @Override
    public void executeBackup() {
        DocumentFile backupDirectory = getWritableBackupDirectoryWithPossibleDefaultValue();
        if (backupDirectory == null) {
            throw new IllegalStateException("Unable to obtain backup directory");
        }

        Map<String, Set<String>> appGroupToPackagesMap = new HashMap<>();
        Set<String> appGroups = appGroupManager.getAllAppGroups();

        for (String appGroup : appGroups) {
            Set<String> packages = appGroupManager.getPackagesOfAppGroup(appGroup);
            appGroupToPackagesMap.put(appGroup, packages);
        }
        Type type = new TypeToken<Map<String, Set<String>>>(){}.getType();
        String appGroupJson = new Gson().toJson(appGroupToPackagesMap, type);

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

    private DocumentFile getWritableBackupDirectoryWithPossibleDefaultValue() {
        DocumentFile backupDirectoryDocumentFile = getPersistedBackupPathDocumentFile();
        if (backupDirectoryDocumentFile != null && backupDirectoryDocumentFile.canWrite()) {
            return backupDirectoryDocumentFile;
        }
        final boolean isLollipopAndAbove = !Constants.DOWNLOAD_DIRECTORY.canWrite();
        // we have a persisted backup directory but not writable
        if (isLollipopAndAbove && backupDirectoryDocumentFile != null) {
            backupConfigService.setBackupPath(null);
            return null;
        // Android versions Lollipop and above which the users get to select the directory
        } else if (isLollipopAndAbove && backupDirectoryDocumentFile == null) {
            return null;
        }

        // versions below 5.0 without Document Tree API, we use the constant download directory
        if (!Constants.BACKUP_DIRECTORY.exists()) {
            Constants.BACKUP_DIRECTORY.mkdirs();
        }
        backupDirectoryDocumentFile = DocumentFile.fromFile(Constants.BACKUP_DIRECTORY);
        Uri backupPathUri = backupDirectoryDocumentFile.getUri();
        backupConfigService.setBackupPath(backupPathUri.toString());
        return backupDirectoryDocumentFile;
    }

    private DocumentFile getPersistedBackupPathDocumentFile() {
        String backupPath = backupConfigService.getBackupPath();
        if (!isBackupPathFormatValid(backupPath)) {
            return null;
        }
        Uri backupPathUri = Uri.parse(backupPath);
        return convertUriToDocumentFile(backupPathUri);
    }

    private DocumentFile convertUriToDocumentFile(Uri uri) {
        DocumentFile documentFile = fromTreeUri(context, uri);
        if (documentFile == null) {
            File file = new File(uri.getPath());
            documentFile = DocumentFile.fromFile(file);
        }
        return documentFile;
    }

    private boolean isBackupPathFormatValid(String backupPath) {
        return backupPath != null && backupPath.length() > 0;
    }

    @Override
    public List<BackupIdentifier> getOrderedBackups() {
        DocumentFile backupDirectory = getWritableBackupDirectoryWithPossibleDefaultValue();
        if (backupDirectory == null) {
            log.warn("Failed to get writable backup directory when getting ordered backups");
            return Collections.emptyList();
        }

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

    private String getBackupFileName() {
        return String.format(BACKUP_FILE_FORMAT, new LocalDate().toString(), System.currentTimeMillis());
    }

    @Override
    public void restore(String backupUniqueId) {
        DocumentFile backupDirectory = getWritableBackupDirectoryWithPossibleDefaultValue();
        if (backupDirectory == null) {
            throw new IllegalStateException("Unable to get writable backup directory when restoring");
        }
        DocumentFile fileToRestore = backupDirectory.findFile(backupUniqueId);
        if (!fileToRestore.canRead()) {
            throw new IllegalStateException("Unable to read file to restore: " + backupUniqueId);
        }
        final InputStream inputStream;
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

    @Override
    public String getHumanReadableBackupPath() {
        DocumentFile backupDirectory = getWritableBackupDirectoryWithPossibleDefaultValue();
        if (backupDirectory == null) {
            return null;
        }
        Uri backupPathUri = backupDirectory.getUri();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return DocumentsContract.getTreeDocumentId(backupPathUri);
        } else {
            return backupPathUri.getPath();
        }
    }

    @Override
    public void setBackupDirectory(Uri backupDirectoryUri) {
        DocumentFile documentFile = convertUriToDocumentFile(backupDirectoryUri);
        if (!documentFile.canWrite()) {
            throw new IllegalStateException(
                    String.format("Selected a backup directory [%s] without write permissions", backupDirectoryUri));
        }
        backupConfigService.setBackupPath(backupDirectoryUri.toString());
    }
}
