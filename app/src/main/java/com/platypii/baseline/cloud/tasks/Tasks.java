package com.platypii.baseline.cloud.tasks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

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
    final List<Task> pending = new ArrayList<>();
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
        synchronized (pending) {
            pending.add(task);
        }
        if (task.taskType().persistent()) {
            // Only need to save if task was persistent
            PendingPreferences.save(context, pending);
        }
        tendQueue();
    }

    private void tendQueue() {
        synchronized (pending) {
            Log.i(TAG, "Tending task queue: " + pending.size() + " tasks");
            Log.d(TAG, "Tending task queue: " + TextUtils.join(",", pending));
            if (running == null && !pending.isEmpty()) {
                // Start first pending task
                running = pending.get(0);
                runAsync();
            }
        }
    }

    /**
     * Run `running` task in a new thread
     */
    private void runAsync() {
        Log.i(TAG, "Running task: " + running);
        new Thread(() -> {
            // Run in background
            try {
                running.run(context);
                // Success
                runSuccess();
            } catch (AuthRequiredException e) {
                // Wait for sign in
                runFailed();
            } catch (Exception e) {
                if (Services.cloud.isNetworkAvailable()) {
                    Log.e(TAG, "Task failed: " + running, e);
                    Exceptions.report(e);
                } else {
                    Log.w(TAG, "Task failed, network unavailable: " + running, e);
                }
                runFailed();
                // TODO: Try again later
            }
        }).start();
    }

    private void runSuccess() {
        Log.i(TAG, "Task success: " + running);
        synchronized (pending) {
            final Task removed = pending.remove(0);
            if (running != removed) {
                Exceptions.report(new IllegalStateException("Invalid pop: " + running + " != " + removed));
            }
            running = null;
            PendingPreferences.save(context, pending);
        }
        // Check for next pending task
        tendQueue();
    }

    private void runFailed() {
        synchronized (pending) {
            running = null;
        }
    }

    /**
     * Remove all tasks of a given type
     */
    public void removeType(@NonNull TaskType taskType) {
        synchronized (pending) {
            final ListIterator<Task> it = pending.listIterator();
            while (it.hasNext()) {
                final Task task = it.next();
                if (task.taskType().name().equals(taskType.name())) {
                    it.remove();
                }
            }
            PendingPreferences.save(context, pending);
        }
    }

    @Override
    public void stop() {
        synchronized (pending) {
            PendingPreferences.save(context, pending);
            pending.clear();
        }
    }

}
