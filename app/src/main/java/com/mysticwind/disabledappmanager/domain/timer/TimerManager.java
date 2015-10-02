package com.mysticwind.disabledappmanager.domain.timer;

public interface TimerManager {
    void schedule(TimesUpObserver timesUpObserver, String uniqueRequestId, long secondsToNotify);
    void cancel(String uniqueRequestId);
}
