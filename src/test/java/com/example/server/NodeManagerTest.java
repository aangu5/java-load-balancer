package com.example.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NodeManagerTest {
    private NodeManager nodeManager;

    @BeforeEach
    void setUp() {
        nodeManager = new NodeManager();
    }

    @Test
    void addAndFindByIPAndPort() throws UnknownHostException {
        Node mockNode = mock(Node.class);
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        when(mockNode.getNodeIPAddress()).thenReturn(addr);
        when(mockNode.getNodePort()).thenReturn(5000);
        when(mockNode.getNodeID()).thenReturn(1);

        nodeManager.addNewNode(mockNode);

        Node found = nodeManager.findByIPAndPort(addr,5000);
        assertNotNull(found);
        assertEquals(5000, found.getNodePort());

        Node notFound = nodeManager.findByIPAndPort(InetAddress.getByName("127.0.0.2"),5000);
        assertNull(notFound);
    }

    @Test
    void getMostFreeNodeAndSort() {
        Node a = mock(Node.class);
        Node b = mock(Node.class);
        Node c = mock(Node.class);

        when(a.getCurrentUtilisation()).thenReturn(10.0);
        when(b.getCurrentUtilisation()).thenReturn(50.0);
        when(c.getCurrentUtilisation()).thenReturn(5.0);

        nodeManager.addNewNode(a);
        nodeManager.addNewNode(b);
        nodeManager.addNewNode(c);

        Node mostFree = nodeManager.getMostFreeNode();
        // most free = lowest utilisation (5.0) is expected to be returned
        assertNotNull(mostFree);
        assertEquals(5.0, mostFree.getCurrentUtilisation());

        // make all nodes full
        when(a.getCurrentUtilisation()).thenReturn(100.0);
        when(b.getCurrentUtilisation()).thenReturn(100.0);
        when(c.getCurrentUtilisation()).thenReturn(100.0);

        Node none = nodeManager.getMostFreeNode();
        assertNull(none);
    }

    @Test
    void deleteRemovesNode() throws UnknownHostException {
        Node mockNode = mock(Node.class);
        InetAddress addr = InetAddress.getByName("127.0.0.1");
        when(mockNode.getNodeIPAddress()).thenReturn(addr);
        when(mockNode.getNodePort()).thenReturn(5001);

        nodeManager.addNewNode(mockNode);
        assertNotNull(nodeManager.findByIPAndPort(addr,5001));

        nodeManager.delete(mockNode);
        assertNull(nodeManager.findByIPAndPort(addr,5001));
    }

    @Test
    void getNextNodeIDIncrements() {
        int id1 = nodeManager.getNextNodeID();
        assertEquals(1, id1);
        Node mockNode = mock(Node.class);
        nodeManager.addNewNode(mockNode);
        int id2 = nodeManager.getNextNodeID();
        assertEquals(2, id2);
    }
}

