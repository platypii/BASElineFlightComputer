package com.platypii.baseline.cloud.tasks;

import android.content.Context;

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class TasksTest {

    @Test
    public void addRemove() {
        Context context = InstrumentationRegistry.getTargetContext();
        Tasks tasks = new Tasks();
        tasks.start(context);
        assertEquals(0, tasks.pending.size());
        TestTask testTask = new TestTask();
        tasks.add(testTask);
        assertEquals(1, tasks.pending.size());
        testTask.wait = false;
        tasks.removeType(TestTask.taskType);
        assertEquals(0, tasks.pending.size());
    }

    @Test
    public void saveRestore() {
        TaskTypes.all.add(TestTask.taskType);
        Context context = InstrumentationRegistry.getTargetContext();
        Tasks tasks = new Tasks();
        tasks.start(context);
        TestTask sillyTask = new TestTask();
        tasks.add(sillyTask);
        tasks.stop();
        tasks.start(context);
        assertEquals(1, tasks.pending.size());
    }
}
