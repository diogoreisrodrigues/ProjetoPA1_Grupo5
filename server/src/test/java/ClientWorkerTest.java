import org.junit.Before;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class ClientWorkerTest {

    private ByteArrayOutputStream outputStream;
    private PrintWriter printWriter;
    private Socket socket;
    ArrayList<ClientWorker> clientWorkers;

    ClientWorker clientworker;

    @BeforeEach
    void setUp() throws IOException {

    }

    @Test
    public void testDisconnectClient() throws IOException, InterruptedException {


        ServerSocket serverSocket = new ServerSocket(8080);
        Socket socket = new Socket("localhost", 8080);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintWriter out = new PrintWriter(outputStream, true);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
        DataInputStream in = new DataInputStream(inputStream);
        Logger logger = Logger.getLogger(ServerThread.class.getName());
        int id = 1;
        ReentrantLock lockLog = new ReentrantLock();
        Queue<Message> buffer = new LinkedList<>();
        Queue<Message> filteredBuffer = new LinkedList<>();
        ReentrantLock bufferLock = new ReentrantLock();
        ReentrantLock filteredBufferLock = new ReentrantLock();
        Queue<String> messageQueue = new LinkedList<>();
        ArrayList<ClientWorker> clientWorkers = new ArrayList<>();

        ReentrantLock lockLogger = new ReentrantLock();
        Semaphore semaphore = new Semaphore(1);
        ClientWorker clientWorker = new ClientWorker(socket, logger, id, lockLog, buffer, filteredBuffer, bufferLock, filteredBufferLock, messageQueue, semaphore);
        clientWorkers.add(clientWorker);
        messageQueue.add("DISCONNECTED Client 1");


        // Invoke the method to be tested
        clientWorker.disconnectClient(clientWorkers,id, messageQueue,socket, out, in);
        sleep(1000);
        // Check the results
        Assertions.assertEquals(0, clientWorkers.size(), "The list of client workers should be empty");
        Assertions.assertEquals("Client 1 has left the chat\n", outputStream.toString(), "The message sent to the other clients is incorrect");
        Assertions.assertTrue(messageQueue.contains("DISCONNECTED Client 1"), "The log entry for disconnecting the client is missing");
        Assertions.assertThrows(IOException.class, in::readUTF, "The input stream should have been closed");
        assertThrows(IOException.class, out::flush, "The output stream should have been closed");
        //assertThrows(IOException.class, socket::close, "The socket should have been closed");
    }


    @Test
    void testSendMessage() {

    }
}
