package phlux;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static phlux.Util.with;
import static phlux.Util.without;

/**
 * This singleton should be a single place where mutable state of the entire application is stored.
 * Don't use the class directly - {@link PhluxScope} is a more convenient and type-safe way.
 */
public enum Phlux {

    INSTANCE;

    private Map<String, Scope> root = Collections.emptyMap();
    private Map<String, List<PhluxStateCallback>> callbacks = Collections.emptyMap();

    public void create(String key, PhluxState initialState) {
        put(key, new Scope(initialState, Collections.<Integer, PhluxBackground>emptyMap()));
    }

    public void restore(String key, Parcelable scope) {
        if (!root.containsKey(key))
            put(key, (Scope) scope);
    }

    public Parcelable get(String key) {
        return root.get(key);
    }

    public void remove(String key) {
        root = without(root, key);
        callbacks = without(callbacks, key);
    }

    public PhluxState state(String key) {
        return root.get(key).state;
    }

    public <S extends PhluxState> void apply(String key, PhluxFunction<S> function) {
        if (root.containsKey(key)) {
            Scope scope = root.get(key);
            S newValue = function.call((S) scope.state);
            root = with(root, key, new Scope(newValue, scope.background));

            for (PhluxStateCallback callback : callbacks.get(key))
                callback.call(newValue);
        }
    }

    public void background(String key, int id, PhluxBackground task) {
        Scope scope = root.get(key);
        root = with(root, key, new Scope(scope.state, with(scope.background, id, task)));
        execute(key, id, task);
    }

    public void drop(String key, int id) {
        Scope scope = root.get(key);
        root = with(root, key, new Scope(scope.state, without(scope.background, id)));
    }

    public void register(String key, PhluxStateCallback callback) {
        List<PhluxStateCallback> cs = callbacks.get(key);
        callbacks = with(callbacks, key, with(cs != null ? cs : Collections.<PhluxStateCallback>emptyList(), callback));
        callback.call(root.get(key).state);
    }

    public void unregister(String key, PhluxStateCallback callback) {
        callbacks = with(callbacks, key, without(callbacks.get(key), callback));
    }

    @Override
    public String toString() {
        return "Phlux{" +
            "root=" + root +
            ", callbacks=" + callbacks +
            '}';
    }

    private void put(String key, Scope scope) {
        root = with(root, key, scope);
        callbacks = with(callbacks, key, Collections.<PhluxStateCallback>emptyList());
        for (Map.Entry<Integer, PhluxBackground> entry : scope.background.entrySet())
            execute(key, entry.getKey(), entry.getValue());
    }

    private <S extends PhluxState> void execute(final String key, final int id, final PhluxBackground<S> entry) {
        entry.execute(new PhluxBackgroundCallback<S>() {
            @Override
            public void call(PhluxFunction<S> function) {
                if (root.containsKey(key)) {
                    apply(key, function);
                }
            }
        }, new PhluxBackgroundDismiss() {
            @Override
            public void call() {
                if (root.containsKey(key)) {
                    Scope scope = root.get(key);
                    root = with(root, key, new Scope(scope.state, without(scope.background, id)));
                }
            }
        });
    }

    static class Scope implements Parcelable {

        final PhluxState state;
        final Map<Integer, PhluxBackground> background;

        Scope(PhluxState state, Map<Integer, PhluxBackground> background) {
            this.state = state;
            this.background = background;
        }

        protected Scope(Parcel in) {
            state = in.readParcelable(PhluxState.class.getClassLoader());
            background = Collections.unmodifiableMap(in.readHashMap(PhluxState.class.getClassLoader()));
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(state, flags);
            dest.writeMap(background);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<Scope> CREATOR = new Creator<Scope>() {
            @Override
            public Scope createFromParcel(Parcel in) {
                return new Scope(in);
            }

            @Override
            public Scope[] newArray(int size) {
                return new Scope[size];
            }
        };

        @Override
        public String toString() {
            return "Scope{" +
                "state=" + state +
                ", background=" + background +
                '}';
        }
    }
}
