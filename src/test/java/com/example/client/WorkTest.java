package com.example.client;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WorkTest {

    @Test
    void run_durationZero_sendsCompleteAndDecrementsClient() {
        Client client = new Client(6010, 2);
        Server server = mock(Server.class);

        Work work = new Work(10, 0, server, client);
        // call run directly to execute synchronously
        work.run();

        // verify that sendMessageToServer was called with COMPLETE,10
        verify(server).sendMessageToServer("COMPLETE,10");
        // client.jobCompleted reduces currentJobs from 0 to -1 in current implementation
        assertEquals(-1, client.getCurrentJobs());
    }
}
