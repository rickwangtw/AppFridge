package com.mysticwind.disabledappmanager.domain.timer;

import android.os.Handler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AndroidHandlerTimerManager implements TimerManager {
    private static final long SECONDS_TO_MILLISECONDS = 1000;

    private final Map<String, Handler> uniqueRequestIdToHandler = new ConcurrentHashMap<>();

    @Override
    public void schedule(final TimesUpObserver timesUpObserver, final String uniqueRequestId, long secondsToNotify) {
        // TODO should we reschedule?
        if (isRequestIdRequested(uniqueRequestId)) {
            log.warn("Request: " + uniqueRequestId + " already scheduled!");
            return;
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                uniqueRequestIdToHandler.remove(uniqueRequestId);
                timesUpObserver.timesUp(uniqueRequestId);
            }
        }, millisecondsFrom(secondsToNotify));
        uniqueRequestIdToHandler.put(uniqueRequestId, handler);
    }

    private boolean isRequestIdRequested(String uniqueRequestId) {
        return uniqueRequestIdToHandler.containsKey(uniqueRequestId);
    }

    @Override
    public void cancel(String uniqueRequestId) {
        if (!isRequestIdRequested(uniqueRequestId)) {
            throw new RuntimeException("No task allocated for request: " + uniqueRequestId);
        }
        Handler handler = uniqueRequestIdToHandler.get(uniqueRequestId);
        handler.removeCallbacksAndMessages(null);
        uniqueRequestIdToHandler.remove(uniqueRequestId);
    }

    private long millisecondsFrom(long seconds) {
        return seconds * SECONDS_TO_MILLISECONDS;
    }
}
