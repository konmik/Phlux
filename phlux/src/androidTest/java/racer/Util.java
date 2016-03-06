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

    public static long now() {
        return System.nanoTime() / 1000000;
    }
}
