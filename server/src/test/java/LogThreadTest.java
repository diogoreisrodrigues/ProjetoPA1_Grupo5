import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

public class LogThreadTest {
    private Queue<String> queueToLog;
    private ReentrantLock lockLog;
    private  ReentrantLock queueLogLock;
    private Logger logger = Logger.getLogger(ServerThread.class.getName());
    private Thread l;
    private Queue<String> testQueue;


    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @BeforeEach
    public void setUp(){
        lockLog = new ReentrantLock();
        queueToLog = new LinkedList<>();
        queueLogLock = new ReentrantLock();
        queueToLog.add("Test Message");
        l  = new LogThread(queueToLog,lockLog,logger,queueLogLock);
        testQueue = new LinkedList<>();

    }

    @Test
    public void testEmptyQueue() throws InterruptedException {
        l.start();
        sleep(1000);
        assertEquals(testQueue.size(),queueToLog.size());
    }

    @Test
    public void testInterruptedException() {
        LogThread logThread = new LogThread(null, null, null, null);
        logThread.interrupt();
        try {
            logThread.join();
        } catch (InterruptedException e) {
            fail();
        }
        assertThrows(RuntimeException.class, () -> {
            logThread.run();
        });
    }
}
