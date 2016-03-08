package racer;

import java.util.concurrent.Semaphore;

public class Util {

    public static void sleep(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void acquire(Semaphore semaphore) {
        try {
            semaphore.acquire();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean waitFor(int milliseconds, Condition condition) {
        long target = System.nanoTime() / 1000000 + milliseconds;
        while (System.nanoTime() / 1000000 < target) {
            if (condition.isTrue())
                return true;
            sleep(1);
        }
        return false;
    }

    public static long now() {
        return System.nanoTime() / 1000000;
    }

    public static void run(int retry, int timeLimit, Iteration iteration) {
        final long time1 = now();
        for (int i = 0; i < retry && now() - time1 < timeLimit; i++)
            iteration.run(i);
    }

    public interface Iteration {
        void run(int iteration);
    }

    public interface Condition {
        boolean isTrue();
    }
}
