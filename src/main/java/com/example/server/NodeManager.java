package com.example.server;

import java.net.InetAddress;
import java.util.Comparator;
import java.util.LinkedList;

public class NodeManager {
    private LinkedList<Node> connectedNodes = new LinkedList<>();           //linked list of all connected Nodes
    private LinkedList<Node> nodesByUtilisation = new LinkedList<>();       //linked list of connected Nodes sorted by utilisation
    private LinkedList<Node> allNodes = new LinkedList<>();                 //linked list of all Nodes

    /**
     * Adds a new node to the linked lists
     * @param newNode - node object representing the new node
     */
    public void addNewNode(Node newNode) {
        if (connectedNodes.add(newNode) && nodesByUtilisation.add(newNode)) {
            allNodes.add(newNode);
        }
    }

    /**
     * returns a node based on a given ip address and port - both used to allow the program to be run on on machine
     * @param ipToFind - ip address of the node to find
     * @param portToFind - port used by the node to find
     * @return - returns the node object or null if not found
     */
    public Node findByIPAndPort(InetAddress ipToFind, int portToFind){
        for (Node connectedNode : connectedNodes) {
            if (connectedNode.getNodeIPAddress().getHostAddress().equals(ipToFind.getHostAddress())) {
                if (connectedNode.getNodePort() == portToFind) {
                    return connectedNode;
                }
            }
        }
        return null;
    }

    /**
     * Sorts the available nodes by their current utilisation - .reversed() is used to bring the most available node to the front of the list
     */
    public void sortNodesByUtilisation() {
        nodesByUtilisation.sort(Comparator.comparingDouble(Node::getCurrentUtilisation).reversed());
    }

    /**
     * Sorts the available nodes then returns the node at the front of the list if it has capacity for more work.
     * If the most free node is full or doesn't exist, the method returns null
     * @return - most free node if there is one available or null if not
     */
    public Node getMostFreeNode() {
        sortNodesByUtilisation();
        if (nodesByUtilisation.isEmpty()) {
            return null;
        } else {
            Node tempNode = nodesByUtilisation.getLast();
            if (tempNode.getCurrentUtilisation()  >= 100) {
                System.out.println("Unable to assign work - all nodes are full!");
                return null;
            } else {
                return tempNode;
            }

        }
    }

    /**
     * removes a Node object from the connectedNodes and nodesByUtilisation linked lists - if machine is not found, message printed to console
     * @param machine - node object of the node to delete
     */
    public void delete (Node machine) {
        try {
            connectedNodes.remove(machine);
            nodesByUtilisation.remove(machine);
        } catch (Exception e){
            System.out.println("Machine not found " + e);
        }
    }

    /**
     * gets the size of allNodes and returns the next integer to act as node ID
     * @return - size of allnodes + 1 for node ID
     */
    public int getNextNodeID() {
        return allNodes.size() + 1;
    }

    /**
     * sends messages to all connected nodes with a shutdown instruction before removing the nodes from the lists
     */
    public void shutdownNodeConnections() {
        for (int i = 0; i < connectedNodes.size(); i++) {
            Node listNode = connectedNodes.get(i);
            listNode.sendMessageToNode("SHUTDOWN");
            connectedNodes.remove(listNode);
            System.out.println("Node " + listNode.getNodeID() + " disconnected.");
        }
    }
}
