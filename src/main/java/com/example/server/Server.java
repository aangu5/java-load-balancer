package com.example.server;

import java.io.IOException;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.ConsoleHandler;
import java.util.logging.SimpleFormatter;
import java.util.logging.Handler;

public class Server {
    private NodeManager nodeManager = new NodeManager();            //instantiates a NodeManager object to store all the Nodes
    private WorkManager workManager = new WorkManager();            //instantiates a WorkManager object to store all the Work
    private int serverPort;                                         //port that the server receives messages on
    private InetAddress serverIP;                                   //IP address of the server, set in the constructor using .getLocalHost()
    boolean systemOnline;                                           //boolean flag that represents whether the system is online or not

    // add logger
    private static final Logger logger = Logger.getLogger(Server.class.getName());

    static {
        // configure logger for this class (console output with simple formatter)
        logger.setUseParentHandlers(false);
        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new SimpleFormatter());
        ch.setLevel(Level.FINE);           // output DEBUG (FINE) and above
        logger.addHandler(ch);
        logger.setLevel(Level.FINE);       // set logger default to DEBUG
    }

    /**
     * Server constructor sets the port, IP and system online and then runs the system with the runSystem() method
     * @param serverPort - integer for the port that the server receives messages on
     */
    public Server(int serverPort) {
        this.serverPort = serverPort;
        systemOnline = true;
        try {
            serverIP = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.log(Level.SEVERE, "Unable to determine local host IP", e);
        }
        runSystem();
    }

    /**
     * @return ip address of server
     */
    public InetAddress getServerIP() {
        return serverIP;
    }

    /**
     * @return port of server
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * initiates a shutdown for the nodes and server if there is no work in progress or backlog
     */
    public void shutdown() {
        logger.info("There is work in progress: " + workManager.isWorkInProgress());
        if (workManager.isWorkInProgress() || workManager.isWorkAvailable()){
            logger.warning("Unable to shutdown due to work in progress or backlog!");
        } else {
            nodeManager.shutdownNodeConnections();
            logger.info("Turning off");
            System.exit(0);
        }
    }

    /**
     * local method to create a new node, calls the Node constructor and adds the new node into the node manager
     * @param nodeIP - IP address of new node
     * @param nodePort - port of new node
     * @param inputMaxJobs - number of max jobs the node can carry out at any one time
     * @return - Node object that is created
     */
    private Node createNewNode(InetAddress nodeIP, int nodePort, int inputMaxJobs) {
        int nodeID = nodeManager.getNextNodeID();
        Node newNode = new Node(this, nodeID, nodeIP, nodePort, inputMaxJobs);
        nodeManager.addNewNode(newNode);
        logger.fine(String.format("Created node object id=%d ip=%s port=%d maxJobs=%d",
                nodeID, nodeIP.getHostAddress(), nodePort, inputMaxJobs));
        return newNode;
    }

    /**
     * local method to create a new Work object, calls the work constructor and adds the new work into the work manager
     * Then tries to assign the work to a node if there is one available, else the work goes into the backlog
     * @param duration - length of time in seconds that the job will take
     */
    public void createNewWork(int duration) {
        int workID = workManager.getNextWorkID();
        Work newWork = new Work(this, workID, duration);
        workManager.addWork(newWork);
        logger.fine(String.format("New work created id=%d duration=%d", workID, duration));
        Node tempNode = assignWorkCreated();
        if (tempNode != null){
            logger.info(String.format("Work %d assigned to node %d", newWork.getWorkID(), tempNode.getNodeID()));
            newWork.setWorkerNode(tempNode);
        } else {
            logger.info(String.format("Work %d not assignable. Adding to backlog.", newWork.getWorkID()));
        }
    }

    /**
     * assigns Work to a Node based on whether work is available and which node is the most free node
     * Then sets the Node to working and sends it a message to start working
     * @return - the Node that the work was assigned to
     */
    private Node assignWorkCreated() {
        String messageToSend;
        if (workManager.isWorkAvailable()) {
            if (nodeManager.getMostFreeNode() == null) {
                logger.fine("Work available but no free node found");
                return null;
            } else {
                Node availableNode = nodeManager.getMostFreeNode();
                Work availableWork = workManager.getAvailableWork();
                messageToSend = "WORK," + availableWork.getWorkID() + "," + availableWork.getDuration();
                workManager.startWork(availableWork);
                logger.info("Sending to node " + availableNode.getNodeID() + ": " + messageToSend);
                availableWork.setWorkerNode(availableNode);
                availableNode.sendMessageToNode(messageToSend);
                availableNode.newJob();
                return availableNode;
            }
        }
        return null;
    }

    /**
     * sets a Work object as complete, calls the setComplete method and removes the task from the node
     * @param workID - the work ID of the Work that was completed
     */
    private void workComplete(int workID){
        Work completedWork = workManager.findByID(workID);
        workManager.workComplete(completedWork);
        completedWork.setComplete(true);
        Node workerNode = completedWork.getWorkerNode();
        if (workerNode != null) {
            workerNode.jobComplete();
            logger.info(String.format("Work %d completed by node %d", workID, workerNode.getNodeID()));
        } else {
            logger.warning(String.format("Work %d completed but no worker node recorded", workID));
        }
    }

    /**
     * Most critical method in this piece of work. Runs the system, opens the serverPort to receive messages and operates a switch case statement based on the message received.
     * Possible messages include:
     * - NEWWORK - used to create new Work objects
     * - SHUTDOWN - used to shutdown the system
     * - NEW - used to create new Node objects when a Node comes online
     * - READY - used to indicated that a Node is ready for work
     * - COMPLETE - used to indicate that work has been completed
     * - FAILEDWORK - used to indicate that work has failed
     * - ALIVE - used to indicate that a Node is alive after the server sends a STATUSCHECK message
     * - WORKING - used to indicate that a Node is working after the server sends a STATUSCHECK message
     * - DEADNODE - used to indicate that the server has detected a deadnode
     * Any other message will be printed to the console with a "I don't understand:" statement and the message
     */
    public void runSystem() {
        logger.info("Running System on port: " + serverPort);
        InetAddress tempNodeIP;
        int tempNodePort;
        int tempMaxJobs;
        try (DatagramSocket socket = new DatagramSocket(serverPort)) {
            socket.setSoTimeout(0);
            logger.info(String.format("Socket opened on %s:%d", InetAddress.getLocalHost().getHostAddress(), serverPort));
            logger.fine("Entering main receive loop");

            while (systemOnline) {
                Node currentNode;
                byte[] buffer = new byte[1024];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                logger.fine(String.format("Packet received from %s:%d (len=%d)", packet.getAddress(), packet.getPort(), packet.getLength()));
                String messages = new String(buffer);
                String[] elements = messages.trim().split(",");
                if (elements.length == 0 || elements[0].trim().isEmpty()) {
                    logger.warning("Received empty or malformed message from " + packet.getAddress() + ":" + packet.getPort() + " -> raw='" + messages.trim() + "'");
                    continue;
                }
                String command = elements[0].trim();
                // log received message with source and parse summary
                logger.fine(String.format("Parsed command='%s' parts=%d raw='%s'", command, elements.length, messages.trim()));
                logger.info(String.format("Received '%s' from %s:%d", messages.trim(), packet.getAddress(), packet.getPort()));
                switch (command) {
                    case "NEWWORK":
                        try {
                            int tempDuration = Integer.parseInt(elements[1]);
                            createNewWork(tempDuration);
                        } catch (NumberFormatException exception) {
                            logger.log(Level.WARNING, "Invalid NEWWORK duration: " + (elements.length > 1 ? elements[1] : "<missing>") + " raw='" + messages.trim() + "'", exception);
                        }
                        break;
                    case "SHUTDOWN":
                        shutdown();
                        break;
                    case "NEW":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            tempMaxJobs = Integer.parseInt(elements[3].trim());
                            currentNode = createNewNode(tempNodeIP, tempNodePort, tempMaxJobs);
                            currentNode.sendMessageToNode("ACCEPTED");
                            logger.info(String.format("New node accepted: %s:%d (id=%d, maxJobs=%d)",
                                    tempNodeIP.getHostAddress(), tempNodePort, currentNode.getNodeID(), tempMaxJobs));
                        } catch (UnknownHostException | NumberFormatException exception) {
                            logger.log(Level.WARNING, "Invalid NEW node parameters raw='" + messages.trim() + "'", exception);
                        }
                        break;
                    case "READY":
                        logger.fine("READY received — attempting to assign work");
                        assignWorkCreated();
                        break;
                    case "COMPLETE":
                        try {
                            int completedWorkID = Integer.parseInt(elements[1].trim());
                            workComplete(completedWorkID);
                        } catch (NumberFormatException exception) {
                            logger.log(Level.WARNING, "Invalid COMPLETE work id: " + (elements.length > 1 ? elements[1] : "<missing>") + " raw='" + messages.trim() + "'", exception);
                        }
                        assignWorkCreated();
                        break;
                    case "FAILEDWORK":
                        try {
                            int tempWorkID = Integer.parseInt(elements[1].trim());
                            Work tempWork = workManager.findByID(tempWorkID);
                            Node badNode = tempWork.getWorkerNode();
                            tempWork.setComplete(true);
                            workManager.updateWork(tempWork);
                            logger.warning("Removing bad node from operation: " + (badNode != null ? badNode.getNodeID() : "unknown"));
                            if (badNode != null) {
                                badNode.sendMessageToNode("SHUTDOWN");
                                nodeManager.delete(badNode);
                                logger.fine("Sent SHUTDOWN to bad node id=" + badNode.getNodeID());
                            }
                            createNewWork(tempWork.getDuration());
                            tempWork.setWorkerNode(null);
                            workManager.workComplete(tempWork);
                        } catch (NumberFormatException exception) {
                            logger.log(Level.WARNING, "Invalid FAILEDWORK work id: " + (elements.length > 1 ? elements[1] : "<missing>") + " raw='" + messages.trim() + "'", exception);
                        }
                        break;
                    case "ALIVE":
                    case "WORKING":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
                            if (currentNode != null) {
                                currentNode.checkNodeIn();
                                logger.fine(String.format("Node check-in: %s:%d (id=%d)", tempNodeIP.getHostAddress(), tempNodePort, currentNode.getNodeID()));
                            } else {
                                logger.warning(String.format("Check-in from unknown node %s:%d", tempNodeIP.getHostAddress(), tempNodePort));
                            }
                        } catch (NumberFormatException | UnknownHostException exception) {
                            logger.log(Level.WARNING, "Invalid ALIVE/WORKING parameters raw='" + messages.trim() + "'", exception);
                        }
                        break;
                    case "DEADNODE":
                        try {
                            tempNodeIP = InetAddress.getByName(elements[1].trim());
                            tempNodePort = Integer.parseInt(elements[2].trim());
                            currentNode = nodeManager.findByIPAndPort(tempNodeIP, tempNodePort);
                            if (currentNode != null) {
                                nodeManager.delete(currentNode);
                                logger.warning("Node deleted due to unresponsiveness: " + currentNode.getNodeID());
                            } else {
                                logger.warning(String.format("DEADNODE for unknown node %s:%d", tempNodeIP.getHostAddress(), tempNodePort));
                            }
                        } catch (NumberFormatException | UnknownHostException exception) {
                            logger.log(Level.WARNING, "Invalid DEADNODE parameters raw='" + messages.trim() + "'", exception);
                        }
                        break;
                    default:
                        logger.warning("I don't understand: " + elements[0] + " raw='" + messages.trim() + "'");
                }
            }
        } catch (IOException exception) {
            logger.log(Level.SEVERE, "I/O error in runSystem", exception);
        }
    }
}
