package com.mysticwind.disabledappmanager.domain.app.model;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApplicationFilter {

    public static final ApplicationFilter DEFAULT =  ApplicationFilter.builder()
            .includeSystemApp(false)
            .build();

    boolean includeSystemApp = false;

}
