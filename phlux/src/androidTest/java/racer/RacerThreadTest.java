package racer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static racer.RacerThread.completed;
import static racer.Util.sleep;

public class RacerThreadTest {

    public class MyList<T> extends ArrayList<T> {
        public void putIfAbsent(T o) {
            if (!contains(o))
                add(o);
        }
    }

    /**
     * Test fails if {@link RacerThreadTest.MyList#putIfAbsent(Object)} does not have synchronized in its declaration.
     */
    @Test
    public void testRace() throws Exception {
        run(100, new Runnable() {
            @Override
            public void run() {
                final MyList<Integer> list = new MyList<>();

                List<RacerThread> threads = new ArrayList<>();

                int threadsNumber = 100;
                for (int i = 0; i < threadsNumber; i++) {
                    final int finalI = i;
                    threads.add(new RacerThread(Arrays.<Runnable>asList(new Runnable() {
                        @Override
                        public void run() {
                            list.putIfAbsent(finalI / 10);
                        }
                    })));
                }

                for (RacerThread thread : threads)
                    thread.next();

                while (!completed(threads))
                    sleep(1);

                assertEquals(10, list.size());
            }
        });
    }

    public static void run(int times, Runnable runnable) {
        for (int i = 0; i < times; i++)
            runnable.run();
    }
}
