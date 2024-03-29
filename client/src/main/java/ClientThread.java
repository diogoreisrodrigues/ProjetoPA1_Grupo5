import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;

/**
 * Class that implements the paralell execution
 * This thread manages the communication with a Client that is connected to the Server
 */
public class ClientThread extends Thread {
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;
    private String username;

    /**
     * This creates a new ClientThread object
     * @param socket is the Client socket connection
     * @param username is the Client username that appears in the chat
     * @throws IOException when occurs an I/O error while creating the input and output streams
     */
    public ClientThread ( Socket socket, String username) throws IOException {

        this.socket = socket;
        this.out = new DataOutputStream ( socket.getOutputStream ( ) );
        this.in = new BufferedReader ( new InputStreamReader( socket.getInputStream ( ) ) );
        this.username = username;


    }

    /**
     * This method is executed when the thread starts
     * Sends the data to the Client and waits for the messages
     */
    public void run ( ) {
        //try {
        waitMessage();
        try {
            // if(sem.tryAcquire(1, TimeUnit.SECONDS)) {
            System.out.println ( "Sending Data" );
            Scanner scanner = new Scanner(System.in);
            while ( socket.isConnected() ) {

                String message = scanner.nextLine();
                out.writeUTF(username+ ": " + message);
                out.flush();

            }
            socket.close();

        } catch ( IOException e ) {
            e.printStackTrace ( );
        }

    }

    /**
     * This method creates a new thread that runs in paralell to receive messages
     * Wait for the messages from the Client and prints them to the console
     */
    public void waitMessage(){
        new Thread(() -> {
            String messageReceived;
            while(socket.isConnected()){
                try{
                    messageReceived = in.readLine();
                    System.out.println(messageReceived);
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }).start();
    }

    /**
     * This is the getter for the output stream
     * @return output stream
     */
    public DataOutputStream getOut() {
        return out;
    }

    /**
     * This is the getter for the buffered reader
     * @return buffered reader in
     */
    public BufferedReader getIn() {
        return in;
    }

    /**
     * This is the getter for socket of the client
     * @return socket of the client
     */
    public Socket getSocket() {
        return socket;
    }

    /**
     * This is the getter for username
     * @return username of the client
     */
    public String getUsername() {
        return username;
    }

    /**
     * Setter for the Buffered Reader in
     * @param in
     */
    public void setIn(BufferedReader in) {
        this.in = in;
    }
}
