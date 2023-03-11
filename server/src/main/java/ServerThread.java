import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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


    public ServerThread ( int port ) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(4);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
        this.counterId = new AtomicInteger(0);
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
        logger.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();
        fh.setFormatter(formatter);
        Thread t = new Thread(() -> {
            while( true ){
                try {
                    socket = server.accept ( );
                    int id = counterId.incrementAndGet();
                    ClientWorker clientWorker = new ClientWorker(socket, logger, id);     //Estou a criar
                    executor.submit(clientWorker);
                } catch(IOException e){
                    throw new RuntimeException();
                }
            }
        });
        t.start();
    }
}
