import java.io.IOException;

public class Main {

    public static void main ( String[] args ) throws IOException {
        ServerThread server = new ServerThread ( 8888 );
        server.start ( );
    } 
}

