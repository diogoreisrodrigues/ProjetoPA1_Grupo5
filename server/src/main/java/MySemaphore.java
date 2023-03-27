import java.util.concurrent.Semaphore;

/**
 *This class is used to override the reducePermits method of Semaphore
 */
public class MySemaphore extends Semaphore {
    public MySemaphore(int permits){
        super(permits);
    }

    /**
     * This class takes the number of reductions and calls the super method to remove it
     * @param reduction the number of permits to remove
     */
    public void reducePermits(int reduction){
        super.reducePermits(reduction);
    }
}

