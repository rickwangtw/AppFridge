package com.mysticwind.disabledappmanager.domain.event;

public interface EventListener<E> {
    void update(E event);
}
