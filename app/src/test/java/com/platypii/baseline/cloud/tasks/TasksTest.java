package com.platypii.baseline.cloud.tasks;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TasksTest {

    @Test
    public void addRemove() {
        Tasks tasks = new Tasks();
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
        TestTask testTask = new TestTask();
        tasks.add(testTask);
        assertEquals(1, tasks.pending.size());
        testTask.wait = false;
        tasks.removeType(TestTask.taskType);
        assertEquals(0, tasks.pending.size());
    }

    @Test
    public void startStop() {
        Tasks tasks = new Tasks();
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
        TestTask testTask = new TestTask();
        tasks.add(testTask);
        assertEquals(1, tasks.pending.size());
        tasks.stop();
        // Tasks are not persisted across start stop
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
    }

}
