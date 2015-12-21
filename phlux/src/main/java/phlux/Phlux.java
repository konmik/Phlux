package phlux;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static phlux.Util.with;
import static phlux.Util.without;

/**
 * This singleton should be a single place where mutable state of the entire application is stored.
 * Don't use the class directly - {@link PhluxScope} is a more convenient way.
 */
public enum Phlux {

    INSTANCE;

    private Map<String, Data.ScopeData> root = Collections.emptyMap();
    private Map<String, List<PhluxStateCallback>> callbacks = Collections.emptyMap();

    public Data.ScopeData get(String key) {
        return root.get(key);
    }

    public void put(String key, Data.ScopeData data) {
        root = with(root, key, data);
    }

    public <S extends PhluxState> void apply(String key, PhluxFunction<S> action) {
        Data.ScopeData data = root.get(key);
        S oldValue = (S) data.state;
        S newValue = action.call(oldValue);
        root = with(root, key, new Data.ScopeData(newValue, data.background));

        for (PhluxStateCallback callback : callbacks.get(key))
            callback.call(newValue);
    }

    public <S extends PhluxState> void execute(final String key, final int id, final Data.BackgroundEntry entry) {
        entry.action.execute(new PhluxBackgroundCallback<S>() {
            @Override
            public void call(PhluxFunction<S> function) {
                Data.ScopeData data = root.get(key);
                if (data.background.values().contains(entry)) {
                    if (!entry.sticky)
                        root = with(root, key, new Data.ScopeData(data.state, without(data.background, id)));
                    apply(key, function);
                }
            }
        });
    }

    public void remove(String key) {
        root = without(root, key);
        callbacks = without(callbacks, key);
    }

    public <S extends PhluxState> void register(String key, PhluxStateCallback<S> callback) {
        List<PhluxStateCallback> cs = callbacks.get(key);
        callbacks = with(callbacks, key, with(cs != null ? cs : Collections.<PhluxStateCallback>emptyList(), callback));
        callback.call((S) root.get(key).state);
    }

    public <S extends PhluxState> void unregister(String key, PhluxStateCallback<S> callback) {
        callbacks = with(callbacks, key, without(callbacks.get(key), callback));
    }

    public <S extends PhluxState> void background(String key, int id, PhluxBackground<S> action, boolean sticky) {
        Data.ScopeData data = root.get(key);
        Data.BackgroundEntry entry = new Data.BackgroundEntry(action, sticky);
        root = with(root, key, new Data.ScopeData(data.state, with(data.background, id, entry)));
        execute(key, id, entry);
    }

    @Override
    public String toString() {
        return "Phlux{" +
            "root=" + root +
            ", callbacks=" + callbacks +
            '}';
    }
}
