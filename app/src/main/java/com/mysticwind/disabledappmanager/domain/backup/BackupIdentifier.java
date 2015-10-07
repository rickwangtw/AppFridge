package com.mysticwind.disabledappmanager.domain.backup;

import org.joda.time.DateTime;

import lombok.Value;

@Value
public class BackupIdentifier {
    private final String uniqueId;
    private final DateTime createdDateTime;
}
