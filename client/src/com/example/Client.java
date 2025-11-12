package com.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Client {
    private InetAddress nodeIPAddress = null;
    private int nodePort = 0;
    private int maxJobs = 0;
    private int currentJobs = 0;

    public Client(int inputPort, int inputJobLimit) {
        try {
            nodeIPAddress = InetAddress.getLocalHost();
            nodePort = inputPort;
            maxJobs = inputJobLimit;
        } catch (UnknownHostException error) {
            error.printStackTrace();
        }
    }
    public int getMaxJobs() { return maxJobs; }
    public int getCurrentJobs() { return currentJobs; }
    public InetAddress getNodeIPAddress() { return nodeIPAddress; }
    public int getNodePort() { return nodePort; }

    public void newJob() {
        currentJobs += 1;
    }
    public void jobCompleted() {
        currentJobs -= 1;
    }

    public boolean getIsWorking() {
        return currentJobs > 0;
    }


}
