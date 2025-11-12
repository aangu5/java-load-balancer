package com.example.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.TimeUnit;

public class Work extends Thread {
    private int workID;                     //unique identifier of the Work object
    private int duration;                   //duration of the Work object in seconds
    private boolean complete;               //indicator of whether the task is complete
    private Node workerNode;                //Node object that is carrying out the Work
    private double timeoutDouble;           //the timeout of the Work object, this is set to time and a half in the constructor
    private Server server;                  //the Server object that is running this program

    public int getWorkID() { return workID; }           //getter for the WorkID
    public int getDuration() { return duration; }       //getter for the duration
    public Node getWorkerNode() { return workerNode; }  //getter for the workerNode

    public void setComplete(boolean inputComplete) { complete = inputComplete; }        //sets the complete field based on the input
    public void setWorkerNode(Node inputWorkerNode) { workerNode = inputWorkerNode; }   //sets the workerNode field based on the input

    /**
     * Constructor for the Work class, sets the variables that are input and prints the the console that work has been created
     * @param server - the Server object that this program is running on
     * @param workID - the workID for this Work object
     * @param duration - the duration in seconds for this Work object
     */
    public Work(Server server, int workID, int duration) {
        try {
            this.server = server;
            this.workID = workID;
            this.duration = duration;
            timeoutDouble = duration + ((double) duration / 2);
            System.out.println("Work created! ID: " + workID + ", duration: " + duration + ".");
        } catch (Exception e) {
            System.out.println("Input not recognised: " + e.getMessage());
        }
    }

    /**
     * this overrides the default run() method in the Thread class to enable this to occur concurrently to the main program.
     * This waits for the job length plus half and if the job is not completed in this time, it will send a message to the main program that the task has failed.
     * Once the server receives it, another task will be created and resubmitted
     */
    @Override
    public void run() {
        try {
            int timeoutInt = (int) timeoutDouble;
            TimeUnit.SECONDS.sleep(timeoutInt);
            if (!complete) {
                System.out.println("Work not completed - potential error!");
                String message = "FAILEDWORK," + workID + "," + duration;
                DatagramPacket packet = new DatagramPacket(message.getBytes(),message.getBytes().length, server.getServerIP(),server.getServerPort());
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
                complete = true;
            }
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
    }
}
