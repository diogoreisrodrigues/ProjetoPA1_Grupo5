import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;
    private final ExecutorService executor;
    private static final Logger logger = Logger.getLogger(ServerThread.class.getName());
    private AtomicInteger counterId;
    private int maxClients;
    private final Semaphore semaphore;
    private final Queue<Socket> waitingClients;

    public ServerThread ( int port ) throws IOException {
        this.port = port;
        this.maxClients = readMaxClientsFromConfig();
        this.executor = Executors.newFixedThreadPool(4);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
        this.semaphore = new Semaphore(maxClients);
        this.counterId = new AtomicInteger(0);
        this.waitingClients = new LinkedList<>();
        try {
            server = new ServerSocket ( this.port );
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
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
        FileHandler fh;
        fh = new FileHandler("server.log");
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    Socket socket = server.accept();
                    semaphore.acquire();

                    int id = counterId.incrementAndGet();

                    ClientWorker clientWorker = new ClientWorker(socket, logger, id, semaphore);
                    executor.submit(clientWorker);

                } catch (IOException | InterruptedException e) {
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

    public void closeServer() throws InterruptedException, IOException {
        executor.shutdown();
        executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

        // Close the server socket and all client sockets
        server.close();
        while (!waitingClients.isEmpty()) {
            Socket socket = waitingClients.poll();
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        }
        logger.info("Server closed");
    }

}
