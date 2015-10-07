package com.mysticwind.disabledappmanager.domain.event;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import de.greenrobot.event.EventBus;

public class EventBusEventManager<E, L extends EventListener<E>> implements EventManager<E, L> {
    protected final EventBus eventBus;
    // leverage WeakHashMap so that we don't have to deal with weak references ourselves
    protected final Set<L> listeners =
            Collections.newSetFromMap(new WeakHashMap<L, Boolean>());

    public EventBusEventManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    public void registerListener(L listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(L listener) {
        listeners.remove(listener);
    }

    @Override
    public void publishUpdate(E event) {
        eventBus.post(event);
    }

    // EventBus
    public void onEventMainThread(E event) {
        for (L listener : listeners) {
            listener.update(event);
        }
    }
}
