package com.platypii.baseline.cloud.tasks;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TasksTest {

    @Test
    public void runTask() throws InterruptedException {
        Tasks tasks = new Tasks();
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
        TestTask testTask = new TestTask("id1");
        tasks.add(testTask);
        assertEquals(1, tasks.pending.size());
        testTask.wait = false;
        Thread.sleep(60);
        assertEquals(0, tasks.pending.size());
    }

    @Test
    public void deDup() throws InterruptedException {
        Tasks tasks = new Tasks();
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
        TestTask testTask1 = new TestTask("id1");
        TestTask testTask2 = new TestTask("id1");
        tasks.add(testTask1);
        tasks.add(testTask2);
        assertEquals(1, tasks.pending.size());
    }

    @Test
    public void removeType() throws InterruptedException {
        Tasks tasks = new Tasks();
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
        TestTask testTask1 = new TestTask("id1");
        TestTask testTask2 = new TestTask("id2");
        tasks.add(testTask1);
        tasks.add(testTask2);
        assertEquals(2, tasks.pending.size());
        tasks.removeType(TestTask.taskType);
        assertEquals(1, tasks.pending.size());
        testTask1.wait = false;
        Thread.sleep(60);
        assertEquals(0, tasks.pending.size());
    }

    @Test
    public void startStop() {
        Tasks tasks = new Tasks();
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
        TestTask testTask = new TestTask("id1");
        tasks.add(testTask);
        assertEquals(1, tasks.pending.size());
        tasks.stop();
        // Tasks are not persisted across start stop
        tasks.start(null);
        assertEquals(0, tasks.pending.size());
    }

}
