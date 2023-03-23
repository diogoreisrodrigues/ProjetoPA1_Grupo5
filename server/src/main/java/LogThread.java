import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

public class LogThread extends Thread{
    private final Queue<String> queueToLog;
    private final ReentrantLock lockLog;
    private final ReentrantLock queueLogLock;
    private final Logger logger;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LogThread(Queue<String> messageQueue, ReentrantLock messageQueueLock, Logger logger) {
        this.queueToLog = messageQueue;
        this.lockLog = messageQueueLock;
        this.logger = logger;
        this.queueLogLock= new ReentrantLock();
    }

    @Override
    public void run(){
        while(true){
            try {
                sleep(500);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if(!queueToLog.isEmpty()){
                queueLogLock.lock();
                String message = queueToLog.poll();
                log(message);
                queueLogLock.unlock();
            }
        }
    }

    public void log ( String message){
        lockLog.lock();
        LocalDateTime timeOfAction = LocalDateTime.now();
        logger.info(timeOfAction.format(formatter)+"- Action : "+ message);
        lockLog.unlock();

    }
}
