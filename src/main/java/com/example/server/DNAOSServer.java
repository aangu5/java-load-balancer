package com.example.server;

public class DNAOSServer {
    /**
     * takes the parameters provided and either runs the system or prints a message to console
     * Expected input is an integer to be used as the server port for incoming messages
     * @param args - should contain a single integer to act as the port for the server to receive messages on
     */

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: Example3 <server port>");
        } else {
            int portNumber = Integer.parseInt(args[0]);
            if (portNumber > 65535) {
                System.out.println("This is too large! Please enter an available port number 1 - 65535");
            } else {
                Server server = new Server(Integer.parseInt(args[0]));
            }
        }
    }
}