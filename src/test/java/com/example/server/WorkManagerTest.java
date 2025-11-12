package com.example.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WorkManagerTest {
    private WorkManager workManager;

    @BeforeEach
    void setUp() {
        workManager = new WorkManager();
    }

    @Test
    void addWorkAndPendingLength() {
        Work w1 = mock(Work.class);
        Work w2 = mock(Work.class);
        when(w1.getDuration()).thenReturn(3);
        when(w2.getDuration()).thenReturn(5);

        workManager.addWork(w1);
        workManager.addWork(w2);

        assertTrue(workManager.isWorkAvailable());
        assertEquals(8, workManager.getPendingWorkLength());
    }

    @Test
    void getAvailableWorkAndNextID() {
        assertNull(workManager.getAvailableWork());
        Work w1 = mock(Work.class);
        when(w1.getDuration()).thenReturn(4);
        workManager.addWork(w1);
        assertNotNull(workManager.getAvailableWork());
        // After adding one work, getNextWorkID should be size+1 => 2
        assertEquals(2, workManager.getNextWorkID());
    }

    @Test
    void findByIDAndWorkComplete() {
        Work w = new Work(null, 7, 10); // server null is fine for inspection
        workManager.addWork(w);
        assertEquals(w, workManager.findByID(7));

        // simulate starting and completing
        workManager.startWork(w);
        assertTrue(workManager.isWorkInProgress());
        workManager.workComplete(w);
        assertFalse(workManager.isWorkInProgress());
    }

    @Test
    void updateWorkReplacesInAllWork() {
        Work original = new Work(null, 1, 2);
        Work updated = new Work(null, 1, 5);
        workManager.addWork(original);
        workManager.updateWork(updated);

        Work found = workManager.findByID(1);
        assertNotNull(found);
        assertEquals(5, found.getDuration());
    }
}
