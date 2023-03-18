import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Scanner;


public class ClientThread extends Thread {


    private DataOutputStream out;
    private BufferedReader in;
    private Socket socket;

    private String username;

    public ClientThread ( Socket socket, String username) throws IOException {

        this.socket = socket;
        this.out = new DataOutputStream ( socket.getOutputStream ( ) );
        this.in = new BufferedReader ( new InputStreamReader ( socket.getInputStream ( ) ) );
        this.username = username;


    }
    
    public void run ( ) {
        //try {
        waitMessage();
            try {
                // if(sem.tryAcquire(1, TimeUnit.SECONDS)) {
                System.out.println ( "Sending Data" );
                Scanner scanner = new Scanner(System.in);
                while ( socket.isConnected() ) {

                    String message = scanner.nextLine();
                    out.writeUTF(message);

                    out.flush();

                }
                socket.close();
                
            } catch ( IOException e ) {
                e.printStackTrace ( );
            }

    }

    public void waitMessage(){
        new Thread(new Runnable(){

            @Override
            public void run() {
                String messageReceived;
                while(socket.isConnected()){
                    try{
                        messageReceived = in.readLine();
                        System.out.println(messageReceived);
                    }catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }).start();
    }
}
