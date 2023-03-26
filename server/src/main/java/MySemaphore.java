import java.util.concurrent.Semaphore;

/**
 *
 */
public class MySemaphore extends Semaphore {
    public MySemaphore(int permits){
        super(permits);
    }

    /**
     * @param reduction the number of permits to remove
     */
    public void reducePermits(int reduction){
        super.reducePermits(reduction);
    }
}

