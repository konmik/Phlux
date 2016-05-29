package phlux;

import android.os.Handler;
import android.os.Looper;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static android.util.Log.i;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static racer.RacerThread.race;
import static racer.Util.run;
import static racer.Util.waitFor;

public class PhluxTest {

    public static final int TEST_RETRIES = 10000;
    public static final int TIME_LIMIT = 10000;
    public static final int THREADS_NUMBER = 10;

    @Test
    public void testLifecycle() throws Exception {

        AtomicReference<TestState> calledBack = new AtomicReference<>();
        StateCallback callback = state -> {
            assertEquals(Looper.myLooper(), Looper.getMainLooper());
            assertTrue(calledBack.compareAndSet(null, (TestState) state));
        };

        String key = UUID.randomUUID().toString();
        Phlux.INSTANCE.create(key, new TestState());
        assertNotNull(Phlux.INSTANCE.state(key));
        Phlux.INSTANCE.register(key, callback);
        Phlux.INSTANCE.apply(key, state -> new TestState());
        assertTrue(waitFor(100, () -> calledBack.get() != null));

        calledBack.set(null);
        AtomicBoolean mainThreadTestDone = new AtomicBoolean();
        new Handler(Looper.getMainLooper()).post(() -> {
            Phlux.INSTANCE.apply(key, state -> new TestState());
            assertNotNull(calledBack.get());
            mainThreadTestDone.set(true);
        });
        assertTrue(waitFor(100, mainThreadTestDone::get));

        Phlux.INSTANCE.unregister(key, callback);
        Phlux.INSTANCE.remove(key);
        assertNull(Phlux.INSTANCE.state(key));
    }

    @Test
    public void testRace() throws Exception {

        AtomicLong calls = new AtomicLong();
        AtomicLong funcs = new AtomicLong();

        run(TEST_RETRIES, TIME_LIMIT, iteration -> {

            List<List<Runnable>> threads = new ArrayList<>();

            for (int i = 0; i < THREADS_NUMBER; i++) {
                String key = UUID.randomUUID().toString();
                threads.add(Arrays.asList(
                    () -> Phlux.INSTANCE.create(key, new TestState()),
                    () -> assertNotNull(Phlux.INSTANCE.state(key)),
                    () -> {
                        calls.incrementAndGet();
                        Phlux.INSTANCE.apply(key, state -> {
                            funcs.incrementAndGet();
                            return new TestState();
                        });
                    },
                    () -> Phlux.INSTANCE.remove(key),
                    () -> assertNull(Phlux.INSTANCE.state(key))));
            }

            race(threads);
        });

        i(getClass().getSimpleName(), "calls: " + calls.get() + " funcs: " + funcs.get() +
            " percent: " + String.format("%.2f", (double) (funcs.get() - calls.get()) / calls.get())); // 4% are normally retries due to STM
        assertNotEquals(calls.get(), funcs.get());
    }
}