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
    private final Semaphore semaphore;

    Queue<Message> buffer = new LinkedList<>();

    Queue<Message> filteredBuffer = new LinkedList<>();

    ReentrantLock bufferLock;

    ReentrantLock filteredBufferLock;

    private final ReentrantLock queueLogLock;



    public ServerThread (int port ) throws IOException {
        this.port = port;
        this.maxClients = readMaxClientsFromConfig();
        this.executor = Executors.newFixedThreadPool(4);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
        this.semaphore = new Semaphore(maxClients);
        this.counterId = new AtomicInteger(0);

        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
        this.lockLog=new ReentrantLock();

        this.bufferLock = new ReentrantLock();
        this.filteredBufferLock = new ReentrantLock();
        this.queueToLog= new LinkedList<>();
        this.queueLogLock= new ReentrantLock();

    }


    /**
     * Explicar Java Doc
     */
    public void run ( ) {
        try {
            setupLogger();
            setupLogThread();
            logger.info("Server started");
            System.out.println ( "Accepting Data" );

            Filter f = startFilter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
            f.start();
            Filter f2 = startFilter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
            f2.start();

            setupMenu();

            acceptClient();
        } catch ( IOException e ) {
            throw new RuntimeException();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void acceptClient() throws IOException, InterruptedException {

        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();


                    int id = counterId.incrementAndGet();

                    if(!semaphore.tryAcquire()){
                        queueLogLock.lock();
                        queueToLog.add("WAITING - CLIENT "+ id);
                        queueLogLock.unlock();
                        semaphore.acquire();
                    }

                    ClientWorker clientWorker = new ClientWorker(socket, logger, id, semaphore,lockLog, buffer, filteredBuffer , bufferLock, filteredBufferLock ,queueToLog);

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


    private void setupLogger() throws IOException {
        Handler[] handlers = logger.getHandlers();
        for(Handler handler : handlers)
        {
            if(handler.getClass() == ConsoleHandler.class)
                logger.removeHandler(handler);
        }
        FileHandler fh;
        fh = new FileHandler("server.log");
        logger.addHandler(fh);

        MyFormatter formatter = new MyFormatter();
        fh.setFormatter(formatter);
        logger.setUseParentHandlers(false);
    }

    private void setupLogThread(){
        LogThread l = new LogThread(queueToLog,lockLog,logger, queueLogLock);
        l.start();
    }

    private void setupMenu(){
        ServerMenu m= new ServerMenu(logger);
        m.start();
    }

    public void closeServer(){
        //TODO: function that ends the server thread
    }


    public Filter startFilter(Queue<Message> buffer, Queue<Message> filteredBuffer, ReentrantLock bufferLock, ReentrantLock filteredBufferLock){
        Filter f= null;
        try {
            f = new Filter(buffer, filteredBuffer, bufferLock, filteredBufferLock);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return f;
    }
}
