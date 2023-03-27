import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Queue;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

/**
 * This class extends Thread, it is responsible for logging messages from the chat application.
 * Messages are stored in a queue and retrieved by this thread to be logged.
 */
public class LogThread extends Thread{
    private final Queue<String> queueToLog;
    private final ReentrantLock lockLog;
    private final ReentrantLock queueLogLock;
    private final Logger logger;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * This is the constructor for the LogThread.
     *
     * @param messageQueue is the queue of messages to log.
     * @param messageQueueLock is the lock used to prevent concurrent access to the message queue.
     * @param logger is the logger object used for logging messages.
     * @param queueLogLock is the lock used to prevent concurrent access to the queue while logging.
     */
    public LogThread(Queue<String> messageQueue, ReentrantLock messageQueueLock, Logger logger, ReentrantLock queueLogLock) {
        this.queueToLog = messageQueue;
        this.lockLog = messageQueueLock;
        this.logger = logger;
        this.queueLogLock= queueLogLock;
    }

    /**
     * This method runs the LogThread instance until the program is finished.
     * The method continuously checks the queueToLog to see if there are any messages to log.
     * If the queue is not empty, the method acquires the queueLogLock.
     * Polls the message from the queue, logs the message with the log method, and releases the lock.
     * If the queue is empty, the method waits for 500 milliseconds before checking the queue again.
     *
     * @throws RuntimeException if there is an InterruptedException while sleeping the thread.
     */
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
    /**
     * Logs the given message with a timestamp.
     *
     * @param message the message to log.
     */
    public void log ( String message){
        lockLog.lock();
        LocalDateTime timeOfAction = LocalDateTime.now();
        logger.info(timeOfAction.format(formatter)+"- Action : "+ message);
        lockLog.unlock();

    }
}
