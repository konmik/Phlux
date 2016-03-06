package racer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Collections.shuffle;
import static racer.Util.acquire;
import static racer.Util.sleep;

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

    public static void race(int threadNumber, Factory factory) {

        List<RacerThread> threads = new ArrayList<>();

        for (int i = 0; i < threadNumber; i++) {
            threads.add(factory.create(i));
        }

        shuffle(threads);

        for (RacerThread thread : threads)
            thread.next();

        while (!completed(threads))
            sleep(1);
    }

    public interface Factory {
        RacerThread create(int threadIndex);
    }
}
