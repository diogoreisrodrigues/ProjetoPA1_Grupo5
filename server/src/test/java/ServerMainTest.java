import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ServerMainTest {
    private ServerThread server;
    private Thread serverThread;


    @Before
    public void setUp() throws Exception {
        server = new ServerThread(8888);
        serverThread = new Thread(server);
        serverThread.start();
    }

    /*
    @After
    public void tearDown() throws Exception {
        server.stopServer();
        serverThread.join();
    }
    */

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

