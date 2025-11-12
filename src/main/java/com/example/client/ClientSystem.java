package com.example.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientSystem extends Thread {

    private static final Logger logger = Logger.getLogger(ClientSystem.class.getName());

    final Server mainNode;
    final Client thisClient;

    public ClientSystem(int clientPort, InetAddress serverIP, int serverPort, int jobLimit) {
        mainNode = new Server(serverIP, serverPort);
        thisClient = new Client(clientPort, jobLimit);
    }

    @Override
    public void run() {
        String messageToSend = "NEW," + thisClient.getNodeIPAddress().getHostAddress() + "," + thisClient.getNodePort() + "," + thisClient.getMaxJobs();
        mainNode.sendMessageToServer(messageToSend);
        logger.log(Level.INFO, "Main Thread: Message sent");

        while (true) {
            String messageReceived = awaitMessageFromServer();
            logger.log(Level.INFO, "Main Thread: {}", messageReceived);
            String[] elements = messageReceived.split(",".trim());
            String inputMessage = elements[0].trim();
            switch (inputMessage) {
                case "ACCEPTED" -> accepted();
                case "WORK" -> work(elements);
                case "SHUTDOWN" -> shutdown();
                case "STATUSCHECK" -> statusCheck();
                default -> unknown(inputMessage);
            }
        }
    }

    private String awaitMessageFromServer() {
        try (DatagramSocket socket = new DatagramSocket(thisClient.getNodePort())){
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.setSoTimeout(0);
            socket.receive(packet);
            return new String(buffer);
        } catch (Exception error) {
            error.printStackTrace();
            return "Error!";
        }
    }

    private void accepted() {
        String messageToSend = "READY";
        mainNode.sendMessageToServer(messageToSend);
    }

    private void work(String[] elements) {
        int tempWorkID = Integer.parseInt(elements[1].trim());
        int tempWorkDuration = Integer.parseInt(elements[2].trim());
        Work newWork = new Work(tempWorkID, tempWorkDuration, mainNode, thisClient);
        thisClient.newJob();
        newWork.start();
    }

    private void shutdown() {
        logger.log(Level.INFO, "Main Thread: Server has issued a SHUTDOWN command, trying to exit the program.");
        System.exit(0);
    }

    private void statusCheck() {
        String messageToSend;
        if (thisClient.getIsWorking()) {
            messageToSend = "WORKING," + thisClient.getNodeIPAddress().getHostAddress() + "," + thisClient.getNodePort() + "," + thisClient.getCurrentJobs() + "," + thisClient.getMaxJobs() + " ";
        } else {
            messageToSend = "ALIVE," + thisClient.getNodeIPAddress().getHostAddress() + "," + thisClient.getNodePort();
        }
        logger.log(Level.INFO, "Message to send is {}", messageToSend);
        mainNode.sendMessageToServer(messageToSend);
    }

    private void unknown(String inputMessage) {
        logger.log(Level.WARNING, "Unknown message received: {}", inputMessage);
    }
}
