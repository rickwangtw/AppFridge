package com.mysticwind.disabledappmanager.common.thread;

import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;

public class RunnableRequestBlockingQueue implements BlockingQueue<Runnable> {
    private PriorityBlockingQueue workQueue;

    private Comparator<TimestampGeneratedRunnable> runnableRequestComparator = new Comparator<TimestampGeneratedRunnable>() {
        @Override
        public int compare(TimestampGeneratedRunnable lhs, TimestampGeneratedRunnable rhs) {
            // prioritize by request time desc
            return lhs.getRequestTimestamp().compareTo(rhs.getRequestTimestamp()) * -1;
        }
    };

    public RunnableRequestBlockingQueue(int initialCapacity) {
        workQueue = new PriorityBlockingQueue<TimestampGeneratedRunnable>(
                initialCapacity, runnableRequestComparator);
    }

    @Override
    public boolean add(Runnable runnable) {
        return workQueue.add(new TimestampGeneratedRunnable(runnable));
    }

    @Override
    public boolean addAll(Collection<? extends Runnable> collection) {
        List<Runnable> runnableRequestList = new ArrayList<>(collection.size());
        Iterator<? extends Runnable> collectionIterator = collection.iterator();
        while (collectionIterator.hasNext()) {
            runnableRequestList.add(new TimestampGeneratedRunnable(collectionIterator.next()));
        }
        return workQueue.addAll(runnableRequestList);
    }

    @Override
    public void clear() {
        workQueue.clear();
    }

    @Override
    public boolean offer(Runnable runnable) {
        return workQueue.offer(new TimestampGeneratedRunnable(runnable));
    }

    @Override
    public Runnable remove() {
        TimestampGeneratedRunnable timestampGeneratedRunnable = (TimestampGeneratedRunnable) workQueue.remove();
        return timestampGeneratedRunnable.getRequest();
    }

    @Override
    public Runnable poll() {
        TimestampGeneratedRunnable timestampGeneratedRunnable = (TimestampGeneratedRunnable) workQueue.poll();
        return timestampGeneratedRunnable.getRequest();
    }

    @Override
    public Runnable element() {
        TimestampGeneratedRunnable timestampGeneratedRunnable = (TimestampGeneratedRunnable) workQueue.element();
        return timestampGeneratedRunnable.getRequest();
    }

    @Override
    public Runnable peek() {
        TimestampGeneratedRunnable timestampGeneratedRunnable = (TimestampGeneratedRunnable) workQueue.peek();
        return timestampGeneratedRunnable.getRequest();
    }

    @Override
    public void put(Runnable runnable) throws InterruptedException {
        workQueue.put(new TimestampGeneratedRunnable(runnable));
    }

    @Override
    public boolean offer(Runnable runnable, long timeout, TimeUnit unit) throws InterruptedException {
        return workQueue.offer(new TimestampGeneratedRunnable(runnable), timeout, unit);
    }

    @Override
    public Runnable take() throws InterruptedException {
        TimestampGeneratedRunnable timestampGeneratedRunnable = (TimestampGeneratedRunnable) workQueue.take();
        return timestampGeneratedRunnable.getRequest();
    }

    @Override
    public Runnable poll(long timeout, TimeUnit unit) throws InterruptedException {
        TimestampGeneratedRunnable timestampGeneratedRunnable = (TimestampGeneratedRunnable) workQueue.poll(timeout, unit);
        return timestampGeneratedRunnable.getRequest();
    }

    @Override
    public int remainingCapacity() {
        return workQueue.remainingCapacity();
    }

    @Override
    public boolean remove(Object o) {
        return workQueue.remove(o);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return workQueue.removeAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return workQueue.retainAll(collection);
    }

    @Override
    public int size() {
        return workQueue.size();
    }

    @NonNull
    @Override
    public Object[] toArray() {
        return workQueue.toArray();
    }

    @NonNull
    @Override
    public <T> T[] toArray(T[] array) {
        return (T[]) workQueue.toArray(array);
    }

    @Override
    public boolean contains(Object o) {
        return workQueue.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return workQueue.containsAll(collection);
    }

    @Override
    public boolean isEmpty() {
        return workQueue.isEmpty();
    }

    @NonNull
    @Override
    public Iterator<Runnable> iterator() {
        return workQueue.iterator();
    }

    @Override
    public int drainTo(Collection<? super Runnable> c) {
        return workQueue.drainTo(c);
    }

    @Override
    public int drainTo(Collection<? super Runnable> c, int maxElements) {
        return workQueue.drainTo(c, maxElements);
    }
}
