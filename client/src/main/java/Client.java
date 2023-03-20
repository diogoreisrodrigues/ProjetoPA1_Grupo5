import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;


/**
 * This class represents the main point for the Chat Client
 * Creates a socket connection to the Server and starts a new ClientThread object
 */
public class Client {

    /**
     * This main method creates a socket connection to yhe Server and starts a new object of ClientThread to handle the communication
     * @param args command line arguments
     * @throws IOException when occurs an I/O error while creating the socket or starting the thread
     */
    public static void main ( String[] args ) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose your username to enter the chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 8888);
        ClientThread client = new ClientThread ( socket, username );
        client.start ( );
    }
}

