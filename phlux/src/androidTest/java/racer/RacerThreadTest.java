package racer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Collections.shuffle;
import static racer.RacerThread.completed;
import static racer.Util.now;
import static racer.Util.sleep;

public class RacerThreadTest {

    public static final int TEST_RETRIES = 10000;
    public static final int TIME_LIMIT = 10000;
    public static final int THREADS_NUMBER = 10;

    public class MyList<T> extends ArrayList<T> {
        public void putIfAbsent(T o) {
            if (!contains(o))
                add(o);
        }
    }

    /**
     * Test fails if {@link RacerThreadTest.MyList#putIfAbsent(Object)} does not have synchronized in its declaration.
     */
    @Test(expected = RacingConditionException.class)
    public void testRace() throws Exception {
        final AtomicInteger retry = new AtomicInteger();
        final long time1 = now();
        run(TEST_RETRIES, TIME_LIMIT, new Runnable() {
            @Override
            public void run() {
                int retryCount = retry.incrementAndGet();
                final MyList<Integer> list = new MyList<>();

                List<RacerThread> threads = new ArrayList<>();
                for (int i = 0; i < THREADS_NUMBER; i++) {
                    final int finalI = i;
                    threads.add(new RacerThread(Arrays.<Runnable>asList(new Runnable() {
                        @Override
                        public void run() {
                            list.putIfAbsent(finalI % 5);
                        }
                    })));
                }
                shuffle(threads);

                for (RacerThread thread : threads)
                    thread.next();

                while (!completed(threads))
                    sleep(1);

                if (list.size() != 5)
                    throw new RacingConditionException("retry: " + retryCount + " during: " + (now() - time1) + "ms");
            }
        });
    }

    private static class RacingConditionException extends RuntimeException {
        public RacingConditionException(String message) {
            super(message);
        }
    }

    public static void run(int retry, int timeLimit, Runnable runnable) {
        final long time1 = now();
        for (int i = 0; i < retry && now() - time1 < timeLimit; i++)
            runnable.run();
    }
}
