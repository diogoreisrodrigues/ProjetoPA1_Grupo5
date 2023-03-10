import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;
    private final ExecutorService executor;
    private static final Logger logger = Logger.getLogger(ServerThread.class.getName());
    private int maxClients;
    private final Semaphore semaphore;

    public ServerThread ( int port ) {
        this.port = port;
        this.maxClients = 2;
        this.semaphore = new Semaphore( maxClients );
        this.executor = Executors.newFixedThreadPool( maxClients );         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
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
        }

    }

    private void acceptClient() throws IOException {
        FileHandler fh;
        fh = new FileHandler("server.log");
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        Queue<Socket> waitingClientsList = new LinkedList<>();
        Thread t = new Thread(() -> {
            while( true ){
                try {
                    semaphore.acquire( );
                    if ( ((ThreadPoolExecutor)executor).getActiveCount() < (maxClients - 1) ){
                        socket = server.accept ( );
                        ClientWorker clientWorker = new ClientWorker(socket, logger);     //Estou a criar
                        executor.submit( clientWorker );
                    } else {
                        waitingClientsList.offer( socket );
                    }
                } catch(IOException | InterruptedException e ){
                    throw new RuntimeException();
                } finally {
                    semaphore.release();
                }
                while ( !waitingClientsList.isEmpty() ){
                    Socket waitingSocket = waitingClientsList.peek();
                    if (((ThreadPoolExecutor) executor).getActiveCount() < (maxClients - 1) ) {
                        waitingClientsList.poll();
                        ClientWorker clientWorker = new ClientWorker(waitingSocket, logger);
                        executor.submit(clientWorker);
                    } else {
                        break;
                    }
                }
            }
        });
        t.start();
    }
}
