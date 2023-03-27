/*import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientTest {

    private PipedInputStream input;
    private PipedOutputStream output;
    private BufferedReader reader;
    private PrintWriter writer;
    private ClientThread clientThread;

    @BeforeEach
    public void setUp() throws Exception {
        // Create a new PipedInputStream and PipedOutputStream
        input = new PipedInputStream();
        output = new PipedOutputStream(input);

        // Create a new BufferedReader and PrintWriter
        reader = new BufferedReader(new InputStreamReader(input));
        writer = new PrintWriter(output, true);

        ServerSocket serverSocket = new ServerSocket(8080);
        Socket socket = new Socket("localhost", 8080);

        // Create a new ClientThread
        clientThread = new ClientThread(socket, "UtilizadorTeste");

        // Set the BufferedReader on the ClientThread
        clientThread.setIn(reader);
    }

    @AfterEach
    public void tearDown() throws Exception {
        // Close the PipedInputStream and PipedOutputStream
        input.close();
        output.close();

        // Close the socket connection to unblock the waitMessage() method
        clientThread.getSocket().close();
    }

    @Test
    public void testWaitMessage() throws IOException {
        // Create a new thread to run the waitMessage() method
        Thread thread = new Thread(clientThread::waitMessage);
        thread.start();

        // Create a ByteArrayInputStream to simulate user input
        ByteArrayInputStream inputStream = new ByteArrayInputStream("mensagem de teste".getBytes());

        // Create a Scanner to read from the ByteArrayInputStream
        Scanner scanner = new Scanner(inputStream);

        // Set the System.in to the ByteArrayInputStream
        System.setIn(inputStream);

        // Send a message to the BufferedReader
        writer.println("mensagem de teste");

        // Wait for the message to be received by the waitMessage() method
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Verify that the message was received by the waitMessage() method
        assertEquals("mensagem de teste", clientThread.getIn().readLine());
    }
}*/