package com.platypii.baseline.util;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

public class PubSub<T> {

    private final List<Subscriber<T>> subs = new ArrayList<>();
    private final List<Subscriber<T>> mainSubs = new ArrayList<>();

    private final Handler handler = new Handler(Looper.getMainLooper());

    public void post(T obj) {
        synchronized (subs) {
            for (Subscriber<T> sub : subs) {
                sub.apply(obj);
            }
        }
        // Run on UI thread
        handler.post(() -> {
            synchronized (mainSubs) {
                for (Subscriber<T> sub : mainSubs) {
                    sub.apply(obj);
                }
            }
        });
    }

    public void subscribe(@NonNull Subscriber<T> sub) {
        synchronized (subs) {
            subs.add(sub);
        }
    }

    public void subscribeMain(@NonNull Subscriber<T> sub) {
        synchronized (mainSubs) {
            mainSubs.add(sub);
        }
    }

    public void unsubscribe(@NonNull Subscriber<T> sub) {
        synchronized (subs) {
            subs.remove(sub);
        }
    }

    public interface Subscriber<S> {
        void apply(S obj);
    }
}