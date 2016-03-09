package racer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static racer.RacerThread.race;
import static racer.Util.now;
import static racer.Util.run;

public class RacerThreadTest {

    public static final int TEST_RETRIES = 10000;
    public static final int TIME_LIMIT = 10000;
    public static final int THREADS_NUMBER = 10;

    public class MyList<T> extends ArrayList<T> {
        public synchronized void putIfAbsent(T o) {
            if (!contains(o))
                add(o);
        }
    }

    /**
     * Test fails if {@link RacerThreadTest.MyList#putIfAbsent(Object)} does not have synchronized in its declaration.
     */
    @Test
    public void testRace() throws Exception {
        long time1 = now();
        run(TEST_RETRIES, TIME_LIMIT, iteration -> {
            MyList<Integer> list = new MyList<>();

            List<List<Runnable>> threads = new ArrayList<>();

            for (int i = 0; i < THREADS_NUMBER; i++) {
                final int finalI = i;
                threads.add(Collections.<Runnable>singletonList(() -> list.putIfAbsent(finalI % 5)));
            }

            race(threads);

            if (list.size() != 5)
                throw new RacingConditionException("iteration: " + iteration + " during: " + (now() - time1) + "ms");
        });
    }
}
