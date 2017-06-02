package com.mysticwind.disabledappmanager.domain.app.model;

import lombok.Value;

@Value
public class ApplicationFilter {
    boolean includeSystemApp = false;
}
