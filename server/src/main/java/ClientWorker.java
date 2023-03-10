import java.io.*;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientWorker implements Runnable{


    private final Socket request;
    //private final FileServer fileServer;
    //private final ReentrantLock lockQueueReplies;
    private final DataInputStream in;
    private final PrintWriter out;

    private byte[] result;

    private static final List<String> bannedWords= new ArrayList<>();

    private AtomicInteger nClients;

    private Queue <Client> queueReplies;

    public ClientWorker (Socket request) {

        try {
            this.request = request;
            this.in = new DataInputStream( request.getInputStream ( ) );
            this.out = new PrintWriter( request.getOutputStream ( ) , true );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void run() {
        while ( true ) {
            try {
                String message = in.readUTF ( );
                Filter f= new Filter(message);
                f.start();
                f.join();
                String filteredMessage =f.getMessage();
                System.out.println ( "***** " + message + " *****" );
                out.println(filteredMessage);

            } catch ( IOException | InterruptedException e ) {
                throw new RuntimeException();
            }
        }
    }

}
