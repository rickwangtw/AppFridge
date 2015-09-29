package com.mysticwind.disabledappmanager.common.thread;

public class TimestampGeneratedRunnable implements Runnable {
    private Runnable request;
    private Long requestTimestamp;

    public TimestampGeneratedRunnable(Runnable request) {
        this.request = request;
        this.requestTimestamp = System.currentTimeMillis();
    }

    public Runnable getRequest() {
        return request;
    }

    public Long getRequestTimestamp() {
        return requestTimestamp;
    }

    @Override
    public void run() {
        request.run();
    }

    @Override
    public boolean equals(Object o) {
        return this.request.equals(o);
    }

    @Override
    public int hashCode() {
        return this.request.hashCode();
    }
}
