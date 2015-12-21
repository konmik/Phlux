package phlux;

import android.os.Bundle;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a keyed access to Phlux methods.
 * {@link PhluxScope} is useful when dealing with independent scopes of data.
 * For example, Activity, Fragment, a custom View can be considered to be scopes.
 */
public class PhluxScope<S extends PhluxState> {

    private final String key;
    private final Phlux phlux = Phlux.INSTANCE;

    /**
     * Constructs a new scope from a given initial state.
     */
    public PhluxScope(S state) {
        this.key = UUID.randomUUID().toString();
        phlux.put(key, new Data.ScopeData(state, Collections.<Integer, Data.BackgroundEntry>emptyMap()));
    }

    /**
     * Restores a scope from a given {@link Bundle}.
     */
    public PhluxScope(Bundle bundle) {
        this.key = bundle.getString("key");
        if (phlux.get(key) == null) {
            Data.ScopeData data = bundle.getParcelable("data");
            phlux.put(key, data);
            for (Map.Entry<Integer, Data.BackgroundEntry> entry : data.background.entrySet())
                phlux.execute(key, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Saves the scope into {@link Bundle}.
     */
    public Bundle save() {
        Bundle bundle = new Bundle();
        bundle.putString("key", key);
        bundle.putParcelable("data", Phlux.INSTANCE.get(key));
        return bundle;
    }

    /**
     * Applies a function to the scope's state.
     */
    public void apply(PhluxFunction<S> action) {
        phlux.apply(key, action);
    }

    /**
     * Executes a background action.
     *
     * Sticky means that the request will not be removed after it's execution and will be re-executed
     * on a process termination.
     */
    public void background(int id, boolean sticky, PhluxBackground<S> action) {
        phlux.background(key, id, action, sticky);
    }

    /**
     * Finally dispose the scope. Do this on Activity.onDestroy when isFinishing() is true.
     * If the scope is controlled by a Fragment then you need to manually control it's existence.
     */
    public void remove() {
        phlux.remove(key);
    }

    /**
     * Registers a callback for state updates.
     * Once registered the callback will be fired immediately.
     */
    public void register(PhluxStateCallback<S> callback) {
        phlux.register(key, callback);
    }

    /**
     * Unregisters a given state callback.
     */
    public void unregister(PhluxStateCallback<S> callback) {
        phlux.unregister(key, callback);
    }
}
