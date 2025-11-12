package com.example;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Node {

    private static final Logger logger = Logger.getLogger(Node.class.getName());

    public static void main(String[] args){

        if(args.length != 4) {
            logger.log(Level.SEVERE, "That is not enough arguments - Sender <client port> <server ip address> <server port> <node job limit>");
        } else {
            try {
                int clientPort = Integer.parseInt(args[0].trim());
                InetAddress tempIP = InetAddress.getByName(InetAddress.getByName(args[1]).getHostAddress());
                int tempJobLimit = Integer.parseInt(args[3].trim());
                int tempPort = Integer.parseInt(args[2].trim());
                ClientSystem client = new ClientSystem(clientPort,tempIP,tempPort, tempJobLimit);
                client.start();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

        }
    }
}
