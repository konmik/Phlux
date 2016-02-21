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

    public void create(String key, PhluxState initialState) {
        put(key, new Scope(initialState));
    }

    public void restore(String key, Parcelable scope) {
        if (!root.containsKey(key))
            put(key, (Scope) scope);
    }

    public Parcelable get(String key) {
        return root.get(key);
    }

    public void remove(String key) {
        if (root.containsKey(key)) {
            Scope scope = root.get(key);
            for (PhluxBackgroundCancellable cancellable : scope.cancellable.values())
                cancellable.cancel();

            root = without(root, key);
        }
    }

    public PhluxState state(String key) {
        return root.get(key).state;
    }

    public <S extends PhluxState> void apply(String key, PhluxFunction<S> function) {
        if (root.containsKey(key)) {
            Scope scope = root.get(key);
            S newValue = function.call((S) scope.state);
            root = with(root, key, new Scope(newValue, scope.callbacks, scope.background, scope.cancellable));

            for (PhluxStateCallback callback : scope.callbacks)
                callback.call(newValue);
        }
    }

    public void background(String key, int id, PhluxBackground task) {
        drop(key, id);
        Scope scope = root.get(key);
        root = with(root, key, new Scope(scope.state, scope.callbacks, with(scope.background, id, task), with(scope.cancellable, id, execute(key, id, task))));
    }

    public void drop(String key, int id) {
        Scope scope = root.get(key);
        if (scope.cancellable.containsKey(id))
            scope.cancellable.get(id).cancel();
        root = with(root, key, new Scope(scope.state, scope.callbacks, without(scope.background, id), without(scope.cancellable, id)));
    }

    public void register(String key, PhluxStateCallback callback) {
        if (root.containsKey(key)) {
            Scope scope = root.get(key);
            root = with(root, key, new Scope(scope.state, with(scope.callbacks, callback), scope.background, scope.cancellable));
            callback.call(scope.state);
        }
    }

    public void unregister(String key, PhluxStateCallback callback) {
        if (root.containsKey(key)) {
            Scope scope = root.get(key);
            root = with(root, key, new Scope(scope.state, without(scope.callbacks, callback), scope.background, scope.cancellable));
        }
    }

    @Override
    public String toString() {
        return "Phlux{" +
            "root=" + root +
            '}';
    }

    private void put(String key, Scope scope) {
        root = with(root, key, scope);
        for (Map.Entry<Integer, PhluxBackground> entry : scope.background.entrySet())
            execute(key, entry.getKey(), entry.getValue());
    }

    private <S extends PhluxState> PhluxBackgroundCancellable execute(final String key, final int id, final PhluxBackground<S> entry) {
        return entry.execute(new PhluxBackgroundCallback<S>() {
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
                    root = with(root, key, new Scope(scope.state, scope.callbacks, without(scope.background, id), without(scope.cancellable, id)));
                }
            }
        });
    }

    static class Scope implements Parcelable {

        final PhluxState state;
        final List<PhluxStateCallback> callbacks;
        final Map<Integer, PhluxBackground> background;
        final Map<Integer, PhluxBackgroundCancellable> cancellable;

        Scope(PhluxState state, List<PhluxStateCallback> callbacks, Map<Integer, PhluxBackground> background, Map<Integer, PhluxBackgroundCancellable> cancellable) {
            this.state = state;
            this.callbacks = callbacks;
            this.background = background;
            this.cancellable = cancellable;
        }

        Scope(PhluxState state) {
            this.state = state;
            this.callbacks = Collections.emptyList();
            this.background = Collections.emptyMap();
            this.cancellable = Collections.emptyMap();
        }

        protected Scope(Parcel in) {
            this.state = in.readParcelable(PhluxState.class.getClassLoader());
            this.callbacks = Collections.emptyList();
            this.background = Collections.unmodifiableMap(in.readHashMap(PhluxState.class.getClassLoader()));
            this.cancellable = Collections.emptyMap();
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
                ", callbacks=" + callbacks +
                ", background=" + background +
                ", cancellable=" + cancellable +
                '}';
        }
    }
}
