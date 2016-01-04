package base;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.LinkedHashMap;
import java.util.concurrent.atomic.AtomicLong;

import phlux.PhluxBackground;
import phlux.PhluxFunction;
import phlux.PhluxScope;
import phlux.PhluxState;
import phlux.PhluxStateCallback;

/**
 * This is an *example* of how to adopt Phlux to your Activities.
 */
public abstract class PhluxActivity<S extends PhluxState> extends Activity {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private PhluxScope<S> scope;
    private boolean registered;
    private AtomicLong updateCounter = new AtomicLong();
    private PhluxStateCallback<S> stateCallback = state -> {
        log("Update " + updateCounter.incrementAndGet());
        update(state);
    };
    private LinkedHashMap<String, Object> updated = new LinkedHashMap<>();
    private boolean updateAllOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scope = savedInstanceState == null ?
            new PhluxScope<>(initial()) :
            new PhluxScope<>(savedInstanceState.getBundle(PHLUX_SCOPE));
    }

    public void apply(PhluxFunction<S> function) {
        scope.apply(function);
    }

    public void background(int id, PhluxBackground<S> background, boolean sticky) {
        scope.background(id, background, sticky);
    }

    public void setUpdateAllOnResume(boolean update) {
        this.updateAllOnResume = update;
    }

    protected abstract S initial();

    protected abstract void update(S state);

    public S state() {
        return scope.state();
    }

    public interface FieldUpdater<T> {
        void call(T value);
    }

    /**
     * Incorporates updating logic for a visual part of an activity,
     * allowing to update only parts of the activity that have updated state.
     * <p>
     * Given we have a deal with immutable state, we only compare
     * references to objects instead of calling equals() to detect
     * changed data.
     */
    public <T> void part(String name, T newValue, FieldUpdater<T> updater) {
        if (!updated.containsKey(name) || updated.get(name) != newValue) {
            log("Update " + updateCounter.get() + " part " + name);
            updater.call(newValue);
            updated.put(name, newValue);
        }
    }

    public void post(Runnable runnable) {
        getWindow()
            .getDecorView()
            .post(runnable);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHLUX_SCOPE, scope.save());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!registered) {
            scope.register(stateCallback);
            registered = true;
        } else if (updateAllOnResume) {
            updated.clear();
            update(state()); // JRebel compatibility
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (registered) {
            scope.unregister(stateCallback);
            registered = false;
        }
        if (isFinishing())
            scope.remove();
    }

    private void log(String message) {
        Log.d(getClass().getSimpleName(), message);
    }
}
