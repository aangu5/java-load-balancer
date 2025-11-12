package com.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Instructor {
    private InetAddress serverIP;       //IP address of the server
    private int serverPort;             //port of the server

    /**
     * Constructor for the Instructor class, stores IP address and port
     * @param serverIP - IP address of the server
     * @param serverPort - port of the server
     */
    public Instructor(InetAddress serverIP, int serverPort) {
        this.serverIP = serverIP;
        this.serverPort = serverPort;
    }

    /**
     * Instantiates a GUI object
     */
    public void runInitiator(){
        GUI screen = new GUI(this);
    }

    /**
     * Method to send work to the Server
     * @param duration - length of work in seconds
     */
    public void sendNewWork(int duration) {
        String message = "NEWWORK," + duration;
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, serverIP, serverPort);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }

    /**
     * Method to send a SHUTDOWN command to the Server
     */
    public void shutdown() {
        String message = "SHUTDOWN";
        try {
            DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getBytes().length, serverIP, serverPort);
            DatagramSocket socket = new DatagramSocket();
            socket.send(packet);
            socket.close();
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
