package com.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {
    /**
     * Takes the arguments from the command line and creates and runs and Instructor object to send Work to the Server
     * @param args - the Server IP address and Server Port - throws error for unknown host
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("That's not the correct amount of arguments! \nArguments: <server IP> <server port> \nExample: 192.168.1.200 5000");
        } else {
            System.out.println("System running :)");
            InetAddress tempIP;
            try {
                tempIP = InetAddress.getByName(args[0]);
                int tempPort = Integer.parseInt(args[1]);
                Instructor host = new Instructor(tempIP, tempPort);
                host.runInitiator();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }
    }
}
