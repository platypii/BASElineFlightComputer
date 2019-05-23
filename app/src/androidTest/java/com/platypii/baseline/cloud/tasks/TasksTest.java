package com.platypii.baseline.cloud.tasks;

import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

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
}
