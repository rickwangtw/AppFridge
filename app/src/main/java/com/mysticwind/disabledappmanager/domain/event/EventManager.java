package com.mysticwind.disabledappmanager.domain.event;

public interface EventManager<E, L extends EventListener<E>> {
    void registerListener(L listener);
    void unregisterListener(L listener);
    void publishUpdate(E event);

}
