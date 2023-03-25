import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class ClientThreadTest {

    DataOutputStream out;
    BufferedReader in;

    @Test
    void testClientThread() throws IOException {
        String username = "teste";
        Socket socket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(socket, username);

        assertAll(
                () -> assertEquals(username, clientThread.username),
                () -> assertEquals(socket, clientThread.socket),
                () -> assertNotNull(clientThread.in),
                () -> assertNotNull(clientThread.out)
        );
    }

    @Test
    public void testWaitMessage() throws IOException, InterruptedException {
        String testMessage = "This is a test message";
        Socket socket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(socket, "User");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        clientThread.waitMessage();

        clientThread.in = new BufferedReader(new StringReader(testMessage));
        Thread.sleep(1000);

        assertTrue(outputStream.toString().contains(testMessage));
    }

    @Test
    public void testWaitMessageIOException() throws InterruptedException, IOException {
        Socket socket = new Socket("localhost", 8888);
        ClientThread clientThread = new ClientThread(socket, "User");

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        clientThread.waitMessage();

        clientThread.in.close();
        Thread.sleep(1000);

        assertThrows(RuntimeException.class, () -> {
            throw new RuntimeException(outputStream.toString());
        });
    }

}
