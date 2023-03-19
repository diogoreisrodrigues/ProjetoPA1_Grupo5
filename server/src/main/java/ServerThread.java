import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
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
    private AtomicInteger counterId;
    private int maxClients;
    private final Semaphore semaphore;
    private final Queue<Socket> waitingClients;

    public ServerThread ( int port ) {
        this.port = port;
        this.maxClients = 2;
        this.executor = Executors.newFixedThreadPool(4);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
        this.semaphore = new Semaphore(maxClients);
        this.counterId = new AtomicInteger(0);
        this.waitingClients = new LinkedList<>();
        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
        this.lockLog=new ReentrantLock();
    }

    /**
     * Explicar Java Doc
     */
    public void run ( ) {
        try {
            logger.info("Server started");
            System.out.println ( "Accepting Data" );
            acceptClient();
        } catch ( IOException e ) {
            throw new RuntimeException();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }

    private void acceptClient() throws IOException, InterruptedException {
        setupLogger();
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();

                    if (semaphore.tryAcquire()) {
                        int id = counterId.incrementAndGet();
                        ClientWorker clientWorker = new ClientWorker(socket, logger, id, semaphore, waitingClients, counterId, executor,lockLog);
                        executor.submit(clientWorker);
                    } else {
                        waitingClients.offer(socket);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                System.out.println(waitingClients);
            }
        });
        t.start();
        t.join();
        Thread t2 = new Thread(() -> {
            while (true) {
                try {
                    if (semaphore.tryAcquire()) {
                        Socket socket = waitingClients.poll();
                        if (socket != null) {
                            int id = counterId.incrementAndGet();
                            ClientWorker clientWorker = new ClientWorker(socket, logger, id, semaphore, waitingClients, counterId, executor,lockLog);
                            executor.submit(clientWorker);
                        }
                    }
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            }
        });
        t2.start();
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

        ConsoleHandler ch = new ConsoleHandler();
        ch.setFormatter(new MyFormatter());
        logger.addHandler(ch);

        MyFormatter formatter = new MyFormatter();
        fh.setFormatter(formatter);
        logger.setUseParentHandlers(false);
    }
    public void closeServer(){
        //TODO: function that ends the server thread
    }

}
