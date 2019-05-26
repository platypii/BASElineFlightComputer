package com.platypii.baseline.cloud.tasks;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Task used just for tests.
 */
public class TestTask implements Task {

    // Task will not complete until this is set to true
    boolean wait = true;

    public static class TestTaskType extends TaskType {
        @NonNull
        @Override
        public String name() {
            return "test-task";
        }
    }
    static final TestTaskType taskType = new TestTaskType();

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
