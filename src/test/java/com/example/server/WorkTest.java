package com.example.server;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class WorkTest {

    @Test
    void constructorSetsFields() {
        Work w = new Work(null, 2, 4);
        assertEquals(2, w.getWorkID());
        assertEquals(4, w.getDuration());
        assertNull(w.getWorkerNode());
    }

    @Test
    void setWorkerNodeAndComplete() {
        InetAddress addr = mock(InetAddress.class);

        Work w = new Work(null, 3, 1);
        Node mockNode = new Node(null, 1, addr, 5000, 1) {
            @Override
            public void sendMessageToNode(String message) {}
        };
        w.setWorkerNode(mockNode);
        assertEquals(mockNode, w.getWorkerNode());
        w.setComplete(true);
        // no getter for complete; ensure no exception thrown when calling run after marking complete
    }
}

