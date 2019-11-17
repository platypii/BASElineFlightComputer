package com.platypii.baseline.cloud.tasks;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Task used just for tests.
 */
public class TestTask extends Task {
    static final TaskType taskType = new TaskType("test-task");

    private final String id;

    // Task will not complete until this is set to true
    boolean wait = true;

    TestTask(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String id() {
        return id;
    }

    @Override
    public void run(@NonNull Context context) throws Exception {
        System.out.println("Running a test task...");
        while (wait) {
            Thread.sleep(50);
            System.out.println("Still running a test task...");
        }
    }

    @NonNull
    @Override
    public TaskType taskType() {
        return taskType;
    }

}
