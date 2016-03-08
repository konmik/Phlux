package racer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;
import static java.util.Collections.shuffle;
import static racer.Util.acquire;
import static racer.Util.sleep;

public class RacerThread {

    private final Semaphore semaphore = new Semaphore(0);
    private final AtomicBoolean completed = new AtomicBoolean();

    public RacerThread(final Iterable<Runnable> list) {
        new Thread(() -> {
            for (Runnable runnable : list) {
                acquire(semaphore);
                runnable.run();
            }
            completed.set(true);
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

    public static void race(List<List<Runnable>> threads) {

        List<RacerThread> racerThreads = new ArrayList<>();

        int maxSteps = 0;
        for (List<Runnable> thread : threads) {
            maxSteps = max(maxSteps, thread.size());
            racerThreads.add(new RacerThread(thread));
        }

        for (int i = 0; i < maxSteps; i++) {
            shuffle(racerThreads);
            for (RacerThread thread : racerThreads)
                thread.next();
        }

        while (!completed(racerThreads))
            sleep(1);
    }
}
