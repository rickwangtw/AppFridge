package com.mysticwind.disabledappmanager.domain.timer;

public interface TimesUpObserver {
    void timesUp(String uniqueRequestId);
}
