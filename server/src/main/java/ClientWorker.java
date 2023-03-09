import java.net.Socket;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

public class ClientWorker implements Runnable{


    private final Socket request;
    //private final FileServer fileServer;
    //private final ReentrantLock lockQueueReplies;

    private byte[] result;

    private AtomicInteger nClients;

    private Queue <Client> queueReplies;

    public ClientWorker (Socket request) {
        this.request = request;
    }


    @Override
    public void run() {

    }
}
