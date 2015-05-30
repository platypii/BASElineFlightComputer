package com.platypii.baseline.data;

import java.util.ArrayList;
import java.util.List;

public class SyncStatus {

    private static List<SyncListener> listeners = new ArrayList<>();

    public static void addListener(SyncListener listener) {
        listeners.add(listener);
    }
    public static void removeListener(SyncListener listener) {
        listeners.remove(listener);
    }

    public static void update() {
        for(SyncListener listener : listeners) {
            listener.syncUpdate();
        }
    }

    public interface SyncListener {
        void syncUpdate();
    }
}
