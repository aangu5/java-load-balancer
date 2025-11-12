package com.example;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class Server {
    private final InetAddress serverIPAddress;
    private final int serverPort;

    public Server(InetAddress inputIP, int inputPort) {
        serverIPAddress = inputIP;
        serverPort = inputPort;
    }

    public void sendMessageToServer(String content) {

        try (DatagramSocket socket = new DatagramSocket()){
            DatagramPacket packet = new DatagramPacket(content.getBytes(), content.getBytes().length, serverIPAddress, serverPort);
            socket.send(packet);
        } catch (Exception error) {
            error.printStackTrace();
        }
    }
}
