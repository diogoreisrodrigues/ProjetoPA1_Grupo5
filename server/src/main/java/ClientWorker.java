import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Authenticator;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.*;

public class ClientWorker implements Runnable{


    private final Socket request;
    //private final FileServer fileServer;
    //private final ReentrantLock lockQueueReplies;
    private final DataInputStream in;
    private final PrintWriter out;

    private final Logger logger;

    private byte[] result;

    private AtomicInteger nClients;

    private Queue <Client> queueReplies;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final ReentrantLock lockLog;

    public ClientWorker (Socket request, Logger logger,ReentrantLock lockLog) {

        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
            this.logger = logger;
            this.lockLog=lockLog;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        try {
        log("CONNECTED ClientX");
        while ( true ) {
                String message = in.readUTF ( );
                if ( message == null) break;
                System.out.println ( "***** " + message + " *****" );
                log("Message - ClientX -  " + message);
                out.println ( "Message received" );
        }
        LocalDateTime logoutTime = LocalDateTime.now();
        log("DISCONNECTED ClientX");
        } catch ( IOException e ) {
            throw new RuntimeException();
        }
    }

    /**
     *Logs the message that is sent in the argument
     * @param message the message to the log
     */
    public void log ( String message){
        try (LockWrapper lock = new LockWrapper(lockLog)) {
            LocalDateTime timeOfAction = LocalDateTime.now();
            logger.info(timeOfAction.format(formatter) + "- Action : " + message);
        }catch (Exception e){

        }

    }
}
