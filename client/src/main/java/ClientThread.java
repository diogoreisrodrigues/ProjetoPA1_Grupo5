import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThread extends Thread {
    private final int port;
    private final int id;
    private final int freq;
    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;



    public ClientThread ( int port , int id , int freq ) {
        this.port = port;
        this.id = id;
        this.freq = freq;

    }

    public void run ( ) {
        //try {
        int i = 0;

            System.out.println ( "Sending Data" );
            try {
                // if(sem.tryAcquire(1, TimeUnit.SECONDS)) {
                socket = new Socket ( "localhost" , port );
                out = new DataOutputStream ( socket.getOutputStream ( ) );
                in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );
                while ( true ) {
                    out.writeUTF("My message number " + i + " to the server testest " + "I'm " + id);
                    String response;
                    response = in.readLine();
                    System.out.println("From Server " + response);
                    out.flush();
                    if(response == null) break;
                    sleep(freq);
                    i++;
                }
                socket.close();
            } catch ( IOException | InterruptedException e ) {
                e.printStackTrace ( );
            }

    }
}
