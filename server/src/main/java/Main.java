import java.io.IOException;

/**
 * This class represents the main point for the Server.
 * Starts a new ServerThread on port 8888, allowing clients to connect and communicate with each other.
 */
public class Main {

    /**
     * The main method creates and starts a new ServerThread on port 8888.
     *
     * @param args is the command line arguments.
     *
     * @throws IOException if an I/O error occurs when creating the ServerThread.
     */
    public static void main ( String[] args ) throws IOException {
        ServerThread server = new ServerThread ( 8888 );
        server.start ( );
    }
}
