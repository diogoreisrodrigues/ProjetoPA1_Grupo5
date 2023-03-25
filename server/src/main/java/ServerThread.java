import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.*;

/**
 * This class extends Thread, and it's responsible for accepting connections, apply the filter and the menu setup.
 */
public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;
    private final ExecutorService executor;
    private static final Logger logger = Logger.getLogger(ServerThread.class.getName());
    private final ReentrantLock lockLog;
    private final Queue<String> queueToLog;
    private AtomicInteger counterId;
    private int maxClients;
    Semaphore semaphore;

    Queue<Message> buffer = new LinkedList<>();

    Queue<Message> filteredBuffer = new LinkedList<>();

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;

    private final ReentrantLock queueLogLock;


    /**
     * This is the constructor for ServerThread.
     * Initializes the server socket on the port that we specify and creates the necessary locks and queues for the server.
     *
     * @param port is the port number for listen to incoming Clients connections.
     * @throws IOException if an I/O error occurs when creating the server socket.
     */
    public ServerThread(int port) throws IOException {
        this.port = port;
        this.maxClients = readMaxClientsFromConfig();
        this.executor = Executors.newFixedThreadPool(4);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
        this.semaphore = new Semaphore(maxClients);
        this.counterId = new AtomicInteger(0);

        try {
            server = new ServerSocket(this.port);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.lockLog = new ReentrantLock();

        this.bufferLock = new ReentrantLock();
        this.filteredBufferLock = new ReentrantLock();
        this.queueToLog = new LinkedList<>();
        this.queueLogLock = new ReentrantLock();

    }


    /**
     * In this method we execute the serve's mains logic.
     * Which consists of setting up the logger and log thread, starting the filters, setting up the menu, and accepting Client connections.
     *
     * @throws RuntimeException if an IO or Interrupted Exception occurs during the execution.
     */
    public void run() {
        try {
            setupLogger();
            setupLogThread();
            logger.info("Server started");
            System.out.println("Accepting Data");

            Filter f = startFilter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
            f.start();
            Filter f2 = startFilter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
            f2.start();

            setupMenu();

            acceptClient();
        } catch (IOException e) {
            throw new RuntimeException();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method accepts client connections and create a new Thread for each Client.
     * If the maximum number of clients has been reached, the method will wait until a spot becomes available.
     *
     * @throws RuntimeException if an IO or Interruped Exception occurs when an error occurs while waiting for a connection.
     */
    private void acceptClient() throws IOException, InterruptedException {

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();


                    int id = counterId.incrementAndGet();

                    if (!semaphore.tryAcquire()) {
                        queueLogLock.lock();
                        queueToLog.add("WAITING - CLIENT " + id);
                        queueLogLock.unlock();
                        semaphore.acquire();
                    }

                    ClientWorker clientWorker = new ClientWorker(socket, logger, id, lockLog, buffer, filteredBuffer, bufferLock, filteredBufferLock, queueToLog, semaphore);

                    executor.submit(clientWorker);

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        t.start();
        t.join();
    }

    /**
     * This method reads the maximum number of Clients allowed to connect to the server from a configuration file.
     *
     * @return the maximum number of clients allowed to connect to yhe server.
     * @throws IOException if occurs an error wile reading the configuration file.
     */
    private int readMaxClientsFromConfig() throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("server.config"));
        String line;
        int maxClients = 0;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("maxClients = ")) {
                maxClients = Integer.parseInt(line.substring("maxClients = ".length()));
            }
        }
        reader.close();
        return maxClients;
    }


    /**
     * This method sets up the logger by removing any existing console handlers, adding a file handler, and setting a custom formatter.
     *
     * @throws IOException if occurs an error while creating the file handler.
     */
    private void setupLogger() throws IOException {
        Handler[] handlers = logger.getHandlers();
        for (Handler handler : handlers) {
            if (handler.getClass() == ConsoleHandler.class)
                logger.removeHandler(handler);
        }
        FileHandler fh;
        fh = new FileHandler("server.log");
        logger.addHandler(fh);

        MyFormatter formatter = new MyFormatter();
        fh.setFormatter(formatter);
        logger.setUseParentHandlers(false);
    }

    /**
     * Starts a new Thread to handle logging messages from the queueToLog buffer.
     * This LogThread instance is created with the provided queueLog, lockLog, logger and queueLogLock parameters.
     */
    private void setupLogThread() {
        LogThread l = new LogThread(queueToLog, lockLog, logger, queueLogLock);
        l.start();
    }

    /**
     * Sets up the Server Menu by creating a new instance of the ServerMenu class and starts it.
     */
    private void setupMenu() {
        ServerMenu m = new ServerMenu(logger);
        m.start();
    }

    public void closeServer() {
        //TODO: function that ends the server thread
    }


    /**
     * This method creates a new filter.
     *
     * @param buffer             is the buffer of incoming messages to filter.
     * @param filteredBuffer     is the buffer of filtered messages to be sent to clients.
     * @param bufferLock         is the lock used to synchronize access to the buffer queue.
     * @param filteredBufferLock is the lock used to synchronize access to the filtered buffer queues.
     * @return the new Filter instance that was created.
     * @throws RuntimeException if an IOException occurs while creating the Filter instance.
     */
    public Filter startFilter(Queue<Message> buffer, Queue<Message> filteredBuffer, ReentrantLock bufferLock, ReentrantLock filteredBufferLock) {
        Filter f = null;
        try {
            f = new Filter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return f;
    }
}
