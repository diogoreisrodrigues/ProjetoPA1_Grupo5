import java.util.concurrent.Semaphore;

public class MySemaphore extends Semaphore {
    public MySemaphore(int permits){
        super(permits);
    }

    //override super class' method
    public void reducePermits(int reduction){
        super.reducePermits(reduction);
    }
}

