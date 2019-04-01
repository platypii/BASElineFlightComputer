package com.platypii.baseline.cloud.tasks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * Queue for tasks such as uploading to the cloud.
 * This class will persist tasks to shared preferences so they can be resumed later.
 * This class tracks state of task execution.
 * Only one task will execute at a time.
 */
public class Tasks implements BaseService {
    private static final String TAG = "Tasks";

    private Context context;

    @NonNull
    private final List<Task> pending = new ArrayList<>();
    @Nullable
    private Task running = null;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        // Load pending from preferences
        pending.addAll(PendingPreferences.load(context));
        // Start pending work
        tendQueue();
    }

    public void add(Task task) {
        Log.i(TAG, "Adding task " + task);
        pending.add(task);
        PendingPreferences.save(context, pending);
        tendQueue();
    }

    private synchronized void tendQueue() {
        Log.i(TAG, "Tending task queue: " + pending.size() + " tasks");
        if (running == null && !pending.isEmpty()) {
            // Start first pending task
            running = pending.get(0);
            Log.i(TAG, "Running task: " + running);
            new Thread(() -> {
                // Run in background
                try {
                    running.run(context);
                    // Success
                    Log.i(TAG, "Task success: " + running);
                    running = null;
                    pending.remove(0);
                    // Check for next pending task
                    tendQueue();
                } catch (Exception e) {
                    Log.e(TAG, "Task failed: " + running, e);
                    running = null;
                    if (Services.cloud.isNetworkAvailable()) {
                        Exceptions.report(e);
                    }
                    // TODO: Try again later
                }
            }).start();
        }
    }

    @Override
    public void stop() {
        PendingPreferences.save(context, pending);
        pending.clear();
    }

}
