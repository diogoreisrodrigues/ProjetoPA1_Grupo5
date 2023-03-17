import java.util.concurrent.locks.Lock;

public class LockWrapper implements AutoCloseable {
    private final Lock lock;

    public LockWrapper( Lock lock) {
        this.lock = lock;
        this.lock.lock();
    }

    @Override
    public void close() {
        lock.unlock();
    }
}