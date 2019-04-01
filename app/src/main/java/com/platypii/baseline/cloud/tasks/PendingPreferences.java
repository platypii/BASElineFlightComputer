package com.platypii.baseline.cloud.tasks;

import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.List;

public class PendingPreferences {

    private static final String PENDING_KEY = "cloud.queue.pending";

    @NonNull
    public static List<Task> load(@NonNull Context context) {
        // Parse from shared preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final String json = prefs.getString(PENDING_KEY, null);
        final List<Task> tasks = new ArrayList<>();
        if (json != null) {
            final List<PendingTask> pendingTasks = new Gson().fromJson(json, new TypeToken<List<PendingTask>>(){}.getType());
            // Convert into tasks
            for (PendingTask pending : pendingTasks) {
                try {
                    tasks.add(TaskTypes.fromJson(pending.name, pending.json));
                } catch (IllegalArgumentException e) {
                    Exceptions.report(e);
                }
            }
        }
        return tasks;
    }

    /**
     * Save pending tasks to shared preferences
     */
    public static void save(@NonNull Context context, @NonNull List<Task> pending) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PENDING_KEY, toJson(pending));
        editor.apply();
    }

    /**
     * Serialize pending tasks as JSON
     */
    @NonNull
    private static String toJson(@NonNull List<Task> pending) {
        final List<PendingTask> list = new ArrayList<>();
        for (Task task : pending) {
            list.add(new PendingTask(task.taskType().name(), task.toJson()));
        }
        return new Gson().toJson(list);
    }


}
