import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerThread extends Thread {
    private final int port;
    private DataInputStream in;
    private PrintWriter out;
    private ServerSocket server;
    private Socket socket;

    private final ExecutorService executor;


    public ServerThread ( int port ) {
        this.port = port;
        this.executor = Executors.newFixedThreadPool(9);         //Por agora nthread ta um numero fixo mas depois corrigir para ficar dinâmico
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

        while ( true ) {
            try {
                System.out.println ( "Accepting Data" );
                acceptClient();


                /*
                in = new DataInputStream ( socket.getInputStream ( ) );
                out = new PrintWriter ( socket.getOutputStream ( ) , true );
                String message = in.readUTF ( );
                System.out.println ( "***** " + message + " *****" );
                out.println ( message.toUpperCase ( ) );
                */
            } catch ( IOException e ) {
                throw new RuntimeException();
            }
        }

    }

    private void acceptClient() throws IOException {

        Thread t = new Thread(() -> {
            while( true ){
                try {
                    socket = server.accept ( );
                    ClientWorker clientWorker = new ClientWorker(socket);     //Estou a criar
                    executor.submit(clientWorker);
                } catch(IOException e){
                    throw new RuntimeException();
                }
            }
        });
        t.start();
    }
}
