package com.example;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class Node extends Thread {
    private InetAddress nodeIPAddress;          //IP address of the Node
    private int nodeID;                         //unique identifier of the Node
    private int nodePort;                       //Port that the Node uses to receive messages on
    private int maxJobs;                        //integer representing the max number of jobs the Node can carry out at once
    private int currentJobs;                    //integer representing the current number of jobs the Node is working on currently
    private long lastCheckIn;                   //long representing the most recent Node check in using Unix time
    private boolean nodeOnline;                 //boolean representing whether the Node is online or not
    private Server server;                      //Server object that this program is running on

    /**
     * Node constructor that sets local variables and outputs to the console that there is a new node added.
     * Then starts the Node check in process.
     * @param inputServer - Server object that this program is running on
     * @param nodeID - unique identifier of this Node
     * @param nodeIPAddress - IP address of this Node
     * @param nodePort - port used to receive messages on this Node
     * @param maxJobs - max number of jobs this Node can carry out at once
     */
    public Node(Server inputServer, int nodeID, InetAddress nodeIPAddress, int nodePort, int maxJobs) {
        this.nodeIPAddress = nodeIPAddress;
        this.nodeID = nodeID;
        this.nodePort = nodePort;
        this.maxJobs = maxJobs;
        lastCheckIn = Instant.now().getEpochSecond();
        nodeOnline = true;
        server = inputServer;
        System.out.println("New machine - ID: " + nodeID + " IP: " + getNodeIPAddress().getHostAddress() + " PORT: " + getNodePort() + " job limit: " + getMaxJobs() );
        start();
    }
    public InetAddress getNodeIPAddress(){ return nodeIPAddress; }                          //getter for Node IP address
    public int getNodeID(){ return nodeID; }                                                //getter for Node ID
    public int getNodePort(){ return nodePort; }                                            //getter for Node Port
    public int getMaxJobs() { return maxJobs; }                                             //getter for Node max jobs
    public double getCurrentUtilisation() {                                                 //getter for current utilisation, calculated by dividing the current jobs by the max jobs
        return ((double)currentJobs /(double) maxJobs) * 100;
    }

    public void newJob() { currentJobs += 1;}                                               //adds one to the current jobs counter
    public void jobComplete() { currentJobs -= 1; }                                         //takes one away from the current jobs counter
    public void checkNodeIn() { lastCheckIn = Instant.now().getEpochSecond(); }             //updates the lastCheckIn to the current second as part of the Node check in process

    /**
     * Sends a message to the Node
     * @param message - String of the message to send to the Node
     */
    public void sendMessageToNode(String message) {
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, getNodeIPAddress(), getNodePort());
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    /**
     * This is the node check in process - once every "timeout" seconds, the server will send a message to the Node asking for a check in.
     * If the Node responds, the check in time gets updated.
     * Then it sleeps for the timeout period and checks whether the Node check in was successful by comparing the current time and the lastCheckIn.
     * If the Node doesn't respond, this function sends a message to the Server to let it know that there is a deadnode.
     */
    @Override
    public void run() {
        while (nodeOnline) {
            System.out.println("Node check in status is: " + lastCheckIn);
            //time delay between Node status checks
            int timeout = 60;
            if (lastCheckIn < (Instant.now().getEpochSecond() - (timeout + timeout / 2))) {
                    System.out.println("Looks like we have a node that isn't responding! Attempting to shutdown the node....");
                    sendMessageToNode("SHUTDOWN");
                    nodeOnline = false;
                    System.out.println("Node not responding - potential error!");
                    String message = "DEADNODE," + nodeIPAddress.getHostAddress() + "," + nodePort;
                    DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, server.getServerIP(),server.getServerPort());
                    DatagramSocket socket;
                    try {
                        socket = new DatagramSocket();
                        socket.send(packet);
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        TimeUnit.SECONDS.sleep(timeout);
                        sendMessageToNode("STATUSCHECK");
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
        }
    }
}
