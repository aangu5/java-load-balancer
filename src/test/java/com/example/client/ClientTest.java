package com.example.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ClientTest {

    @Test
    void constructorAndGetters() {
        Client c = new Client(6000, 4);
        assertEquals(4, c.getMaxJobs());
        assertEquals(0, c.getCurrentJobs());
        assertNotNull(c.getNodeIPAddress());
        assertEquals(6000, c.getNodePort());
    }

    @Test
    void newJobAndJobCompletedAndIsWorking() {
        Client c = new Client(6001, 2);
        assertFalse(c.getIsWorking());
        c.newJob();
        assertEquals(1, c.getCurrentJobs());
        assertTrue(c.getIsWorking());
        c.jobCompleted();
        assertEquals(0, c.getCurrentJobs());
        assertFalse(c.getIsWorking());
    }

    @Test
    void jobCompletedBoundary() {
        Client c = new Client(6002, 1);
        // jobCompleted when zero currently will decrement into negative - current behavior is to allow negative
        c.jobCompleted();
        assertEquals(-1, c.getCurrentJobs());
    }
}
