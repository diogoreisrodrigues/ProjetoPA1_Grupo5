import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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

    private final int id;

    public ClientWorker (Socket request, Logger logger, int id) {

        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
            this.logger = logger;
            this.id = id;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void run() {
        try {
        LocalDateTime loginTime = LocalDateTime.now();
        logger.info(loginTime.format(formatter)+"- Action : CONNECTED Client "+id);
        while ( true ) {
                String message = in.readUTF ( );
                if ( message == null) break;
                System.out.println ( "***** " + message + " *****" );
                LocalDateTime messageTime = LocalDateTime.now();
                logger.info(messageTime.format(formatter)+"- Action : Message - Client "+id +" -  "+message);
                out.println ( "Message received" );
        }
        LocalDateTime logoutTime = LocalDateTime.now();
        logger.info(logoutTime.format(formatter)+"- Action : DISCONNECTED Client "+id);
        } catch ( IOException e ) {
            throw new RuntimeException();
        }
    }

}
