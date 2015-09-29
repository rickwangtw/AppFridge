package com.mysticwind.disabledappmanager.common.thread;

import android.support.annotation.NonNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class RequestStackThreadPoolExecutor implements ExecutorService {
    private final ThreadPoolExecutor threadPoolExecutor;

    public RequestStackThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                          long keepAliveTime, TimeUnit unit) {
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize,
                keepAliveTime, unit, new RunnableRequestBlockingQueue(maximumPoolSize));
    }

    @Override
    public void shutdown() {
        threadPoolExecutor.shutdown();
    }

    @NonNull
    @Override
    public List<Runnable> shutdownNow() {
        return threadPoolExecutor.shutdownNow();
    }

    @Override
    public boolean isShutdown() {
        return threadPoolExecutor.isShutdown();
    }

    @Override
    public boolean isTerminated() {
        return threadPoolExecutor.isTerminated();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return threadPoolExecutor.awaitTermination(timeout, unit);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return threadPoolExecutor.submit(task);
    }

    @NonNull
    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return threadPoolExecutor.submit(task, result);
    }

    @NonNull
    @Override
    public Future<?> submit(Runnable task) {
        return threadPoolExecutor.submit(task);
    }

    @NonNull
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return threadPoolExecutor.invokeAll(tasks);
    }

    @NonNull
    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return threadPoolExecutor.invokeAll(tasks, timeout, unit);
    }

    @NonNull
    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return threadPoolExecutor.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return threadPoolExecutor.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        threadPoolExecutor.execute(command);
    }
}
