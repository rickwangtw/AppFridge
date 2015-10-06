package com.mysticwind.disabledappmanager.domain.state;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import de.greenrobot.event.EventBus;

public class EventBusManualStateUpdateEventManager implements ManualStateUpdateEventManager {
    private final EventBus eventBus;
    // leverage WeakHashMap so that we don't have to deal with weak references ourselves
    private final Set<ManualStateUpdateListener> listeners =
            Collections.newSetFromMap(new WeakHashMap<ManualStateUpdateListener, Boolean>());

    public EventBusManualStateUpdateEventManager(EventBus eventBus) {
        this.eventBus = eventBus;
        this.eventBus.register(this);
    }

    @Override
    public void registerListener(ManualStateUpdateListener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(ManualStateUpdateListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void publishUpdate(String packageName, PackageState packageState) {
        ManualStateUpdate update = new ManualStateUpdate(packageName, packageState);
        eventBus.post(update);
    }

    // EventBus
    public void onEventMainThread(ManualStateUpdate manualStateUpdate) {
        for (ManualStateUpdateListener listener : listeners) {
            listener.update(manualStateUpdate);
        }
    }
}
