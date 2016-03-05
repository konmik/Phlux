package racer;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static racer.Util.acquire;

public class RacerThread {

    private final Semaphore semaphore = new Semaphore(0);
    private final AtomicBoolean completed = new AtomicBoolean();

    public RacerThread(final Iterable<Runnable> list) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (Runnable runnable : list) {
                    acquire(semaphore);
                    runnable.run();
                }
                completed.set(true);
            }
        }).start();
    }

    public void next() {
        semaphore.release();
    }

    public boolean completed() {
        return completed.get();
    }

    public static boolean completed(Iterable<RacerThread> threads) {
        for (RacerThread thread : threads) {
            if (!thread.completed())
                return false;
        }
        return true;
    }
}
