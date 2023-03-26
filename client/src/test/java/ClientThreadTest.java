import static org.junit.jupiter.api.Assertions.*;

import org.junit.Before;
import org.junit.After;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ClientThreadTest {
    private ServerSocket serverSocket;
    private Socket socket;
    private ClientThread clientThread;
    DataOutputStream out;
    BufferedReader in;

    private ServerThread serverThread;

    @BeforeEach
    public void setup() throws IOException {
        serverThread = new ServerThread(8888);
        serverThread.start();
    }

    @AfterEach
    public void cleanup() throws IOException, InterruptedException {
        serverThread.interrupt();
        serverThread.join();
    }

    @Test
    void testClientThread() throws IOException {
        String username = "teste";
        Socket socket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(socket, username);

        assertAll(
                () -> assertEquals(username, clientThread.getUsername()),
                () -> assertEquals(socket, clientThread.getSocket()),
                () -> assertNotNull(clientThread.getIn()),
                () -> assertNotNull(clientThread.getOut())
        );
    }

    @Test
    public void testWaitMessage() throws IOException, InterruptedException {
        String testMessage = "This is a test message";
        Socket socket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(socket, "User");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        clientThread.waitMessage();

        clientThread.setIn(new BufferedReader(new StringReader(testMessage)));

        Thread.sleep(1000);

        assertTrue(outputStream.toString().contains(testMessage));
    }

    @Test
    public void testWaitMessageIOException() throws InterruptedException, IOException {
        Socket socket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(socket, "User");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        clientThread.waitMessage();

        clientThread.getIn().close();

        Thread.sleep(1000);

        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException(outputStream.toString());
        });
    }

    @Test
    void testRun() throws IOException, InterruptedException {
        ServerSocket serverSocket = new ServerSocket(8080);
        Thread serverThread = new Thread(() -> {
            try {
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                String received = in.readLine();
                assertEquals("testuser: test message", received);
            } catch (IOException e) {
                fail("IOException: " + e.getMessage());
            }
        });
        serverThread.start();

        Socket clientSocket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(clientSocket, "testuser");

        String input = "test message\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        System.setIn(inputStream);

        clientThread.start();
        clientThread.join();

        clientSocket.close();
        serverSocket.close();
        serverThread.join();
    }

}