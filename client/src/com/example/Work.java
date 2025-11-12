package com.example;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Work extends Thread {

    private static final Logger logger = Logger.getLogger(Work.class.getName());

    private int workID;
    private int duration;
    private Client thisNode;
    private Server mainNode;

    public Work(int workID, int inputDuration, Server inputServer, Client inputClient){
        try {
            thisNode = inputClient;
            mainNode = inputServer;
            this.workID = workID;
            duration = inputDuration;
            logger.log(Level.INFO, "Work Thread: Work created! ID: {}, duration: {}", new Object[] {workID, duration});
        } catch (Exception e) {
            logger.log(Level.WARNING, "Input not recognised: {}", e.getMessage());
        }
    }

    private void workComplete() {
        String messageToSend = "COMPLETE," + workID;
        mainNode.sendMessageToServer(messageToSend);
        thisNode.jobCompleted();
    }

    @Override
    public void run() {
        for (int i = 0; i < duration; i++){
            logger.log(Level.INFO, "Work Thread: Task {} progress: {}/{}", new Object[] { workID, i, duration });
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        logger.log(Level.INFO, "Work Thread: Task {} is complete", workID);
        workComplete();
    }
}
