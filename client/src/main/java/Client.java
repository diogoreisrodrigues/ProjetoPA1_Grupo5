import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    public static void main ( String[] args ) throws IOException {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose your username to enter the chat: ");
        String username = scanner.nextLine();
        Socket socket = new Socket("localhost", 8888);
        ClientThread client = new ClientThread ( socket, username );
        client.start ( );
    }
}
