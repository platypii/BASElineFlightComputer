package com.platypii.baseline.cloud.tasks;

import android.content.Context;

import androidx.annotation.NonNull;

/**
 * Task used just for tests.
 */
public class TestTask implements Task {

    // Task will not complete until this is set to true
    boolean wait = true;

    public static class TestTaskType implements TaskType {
        @Override
        public String name() {
            return "test-task";
        }
        @Override
        public Task fromJson(@NonNull String json) {
            return null;
        }
        @Override
        public boolean persistent() {
            return true;
        }
    }
    public static TestTaskType taskType = new TestTaskType();

    @Override
    public void run(@NonNull Context context) throws Exception {
        System.out.println("Running a test task...");
        while (wait) {
            Thread.sleep(50);
            System.out.println("Still running a test task...");
        }
    }

    @Override
    public TaskType taskType() {
        return new TestTaskType();
    }

    @Override
    public String toJson() {
        return null;
    }

}
