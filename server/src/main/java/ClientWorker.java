import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
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

    private static final List<String> bannedWords= new ArrayList<>();

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

        log( "CONNECTED Client "+id);

        while ( true ) {
          try {
                String message = in.readUTF ( );
                if ( message == null) break;
                Filter f= new Filter(message);
                f.start();
                f.join();
                String filteredMessage =f.getMessage();
                System.out.println ( "***** " + message + " *****" );
                out.println(filteredMessage);
                log("Message - Client "+id +" -  "+message);
                out.println ( "Message received" );

               } catch ( IOException | InterruptedException e ) {
                throw new RuntimeException();
            }
        }

        log("DISCONNECTED Client "+id);

    }

    public void log ( String message){
        //lock
        LocalDateTime timeOfAction = LocalDateTime.now();
        logger.info(timeOfAction.format(formatter)+"- Action : "+ message);
        //unlock
    }

}
