/*

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;


public class ServerMainTest {
    private ServerThread server;
    private Thread serverThread;


    @BeforeEach
    public void setUp() throws Exception {
        server = new ServerThread(8888);
        serverThread = new Thread(server);
        serverThread.start();
    }


    @AfterEach
    public void tearDown() throws Exception {
        serverThread.join();
    }


    @Test
    public void testServerConnection() {
        try {
            // connect to the server
            Socket socket = new Socket("localhost", 8888);

            // assert that the connection was successful
            assertTrue(serverThread.isAlive());

            // close the socket
            socket.close();
        } catch (IOException e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
*/
