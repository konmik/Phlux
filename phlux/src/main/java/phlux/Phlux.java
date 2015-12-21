package phlux;

import android.os.Parcelable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import auto.parcel.AutoParcel;

import static phlux.Fn.with;
import static phlux.Fn.without;

/**
 * This singleton should be a single place where mutable state of the entire application is stored.
 * Don't use the class directly - {@link PhluxScope} is a more convenient way.
 */
public enum Phlux {

    INSTANCE;

    private Map<String, ScopeData> root = Collections.emptyMap();
    private Map<String, List<PhluxCallback>> callbacks = Collections.emptyMap();

    public ScopeData get(String key) {
        return root.get(key);
    }

    public void put(String key, ScopeData data) {
        root = with(root, key, data);
    }

    public <S extends PhluxState> void apply(String key, PhluxFunction<S> action) {
        ScopeData data = root.get(key);
        S oldValue = (S) data.state();
        S newValue = action.call(oldValue);
        root = with(root, key, ScopeData.create(newValue, data.background()));

        for (PhluxCallback callback : callbacks.get(key))
            callback.call(newValue);
    }

    public <S extends PhluxState> void execute(final String key, final int id, final Phlux.BackgroundEntry entry) {
        entry.action().execute(new PhluxBackgroundCallback<S>() {
            @Override
            public void call(PhluxFunction<S> function) {
                Phlux.ScopeData data = root.get(key);
                if (data.background().values().contains(entry)) {
                    if (!entry.sticky())
                        root = with(root, key, Phlux.ScopeData.create(data.state(), without(data.background(), id)));
                    apply(key, function);
                }
            }
        });
    }

    public void remove(String key) {
        root = without(root, key);
        callbacks = without(callbacks, key);
    }

    public <S extends PhluxState> void register(String key, PhluxCallback<S> callback) {
        List<PhluxCallback> cs = callbacks.get(key);
        callbacks = with(callbacks, key, with(cs != null ? cs : Collections.<PhluxCallback>emptyList(), callback));
        callback.call((S) root.get(key).state());
    }

    public <S extends PhluxState> void unregister(String key, PhluxCallback<S> callback) {
        callbacks = with(callbacks, key, without(callbacks.get(key), callback));
    }

    public <S extends PhluxState> void background(String key, int id, PhluxBackground<S> action, boolean sticky) {
        Phlux.ScopeData data = root.get(key);
        Phlux.BackgroundEntry entry = Phlux.BackgroundEntry.create(action, sticky);
        root = with(root, key, Phlux.ScopeData.create(data.state(), with(data.background(), id, entry)));
        execute(key, id, entry);
    }

    @AutoParcel
    static abstract class BackgroundEntry implements Parcelable {

        abstract PhluxBackground action();
        abstract boolean sticky();

        static BackgroundEntry create(PhluxBackground action, boolean sticky) {
            return new AutoParcel_Phlux_BackgroundEntry(action, sticky);
        }
    }

    @AutoParcel
    static abstract class ScopeData implements Parcelable {

        abstract PhluxState state();
        abstract Map<Integer, BackgroundEntry> background();

        static ScopeData create(PhluxState state, Map<Integer, BackgroundEntry> background) {
            return new AutoParcel_Phlux_ScopeData(state, background);
        }
    }
}
