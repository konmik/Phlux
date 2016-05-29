package phlux;

import android.os.Handler;
import android.os.Looper;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static phlux.Util.with;
import static phlux.Util.without;

/**
 * This singleton should be a single place where mutable state of the entire application is stored.
 * Don't use the class directly - {@link phlux.Scope} is a more convenient and type-safe way.
 */
public enum Phlux {

    INSTANCE;

    private static final int STM_MAX_TRY = 99;

    private AtomicReference<Map<String, Scope>> root = new AtomicReference<>(Collections.<String, Scope>emptyMap());
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public void create(final String key, final ViewState initialState) {
        ScopeTransactionResult result = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return new Scope(initialState);
            }
        });
        for (Map.Entry<Integer, Background> entry : result.now.background.entrySet())
            execute(key, entry.getKey(), entry.getValue());
    }

    public void restore(final String key, final Parcelable scope) {
        ScopeTransactionResult result = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope1) {
                return scope1 == null ? (Scope) scope : scope1;
            }
        });
        if (result.prev == null) {
            for (Map.Entry<Integer, Background> entry : result.now.background.entrySet())
                execute(key, entry.getKey(), entry.getValue());
        }
    }

    public Parcelable get(String key) {
        return root.get().get(key);
    }

    public ViewState state(String key) {
        Scope scope = root.get().get(key);
        return scope == null ? null : scope.state;
    }

    public void remove(final String key) {
        ScopeTransactionResult result = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return null;
            }
        });
        if (result.prev != null) {
            for (Cancellable cancellable : result.prev.cancellable.values())
                cancellable.cancel();
        }
    }

    @Nullable
    public <S extends ViewState> ApplyResult<S> apply(final String key, final Function<S> function) {
        ScopeTransactionResult result = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return scope == null ? null : scope.withState(function.call((S) scope.state));
            }
        });
        if (result.now != null)
            callback(key, result.now);
        return result.prev == null ? null : new ApplyResult(result.prev.state, result.now.state);
    }

    public void background(String key, final int id, final Background task) {
        ScopeTransactionResult result = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return scope == null ? null : scope
                    .withBackground(with(scope.background, id, task))
                    .withCancellable(without(scope.cancellable, id));
            }
        });
        if (result.prev != null && result.prev.cancellable.containsKey(id))
            result.prev.cancellable.get(id).cancel();

        final Cancellable cancellable = execute(key, id, task);

        ScopeTransactionResult result2 = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return scope == null ? null :
                    scope.background.get(id) == task ? scope.withCancellable(with(scope.cancellable, id, cancellable)) : scope;
            }
        });
        if (result2.now == result2.prev)
            cancellable.cancel();
    }

    public void drop(String key, final int id) {
        ScopeTransactionResult result = swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return scope == null ? null : scope
                    .withBackground(without(scope.background, id))
                    .withCancellable(without(scope.cancellable, id));
            }
        });
        if (result.prev != null && result.prev.cancellable.containsKey(id))
            result.prev.cancellable.get(id).cancel();
    }

    public void register(String key, final StateCallback callback) {
        swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return scope == null ? null : scope.withCallbacks(with(scope.callbacks, callback));
            }
        });
    }

    public void unregister(String key, final StateCallback callback) {
        swapScope(key, new ScopeTransaction() {
            @Override
            public Scope transact(Scope scope) {
                return scope == null ? null : scope.withCallbacks(without(scope.callbacks, callback));
            }
        });
    }

    private void callback(final String key, final Scope scope) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            for (StateCallback callback : scope.callbacks)
                callback.call(scope.state);
        }
        else {
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Scope current = root.get().get(key);
                    if (current != null && current.state == scope.state) {
                        for (StateCallback callback : current.callbacks)
                            callback.call(scope.state);
                    }
                }
            });
        }
    }

    private static class ScopeTransactionResult extends ApplyResult<Scope> {
        private ScopeTransactionResult(String key, ApplyResult<Map<String, Scope>> result) {
            super(result.prev.get(key), result.now.get(key));
        }
    }

    private ScopeTransactionResult swapScope(final String key, final ScopeTransaction transaction) {
        return new ScopeTransactionResult(key, swap(new Transaction() {
            @Override
            public Map<String, Scope> transact(Map<String, Scope> root) {
                Scope scope = transaction.transact(root.get(key));
                return scope == null ? without(root, key) : with(root, key, scope);
            }
        }));
    }

    private ApplyResult<Map<String, Scope>> swap(Transaction transaction) {
        int counter = 0;
        ApplyResult<Map<String, Scope>> newValue;
        while ((newValue = tryTransaction(root, transaction)) == null) {
            if (++counter > STM_MAX_TRY)
                throw new IllegalStateException("Are you doing time consuming operations during Phlux apply()?");
        }
        return newValue;
    }

    private ApplyResult<Map<String, Scope>> tryTransaction(AtomicReference<Map<String, Scope>> ref, Transaction transaction) {
        Map<String, Scope> original = ref.get();
        Map<String, Scope> newValue = transaction.transact(original);
        return ref.compareAndSet(original, newValue) ? new ApplyResult<>(original, newValue) : null;
    }

    @Override
    public String toString() {
        return "Phlux{" +
            "root=" + root +
            '}';
    }

    private <S extends ViewState> Cancellable execute(final String key, final int id, final Background<S> entry) {
        return entry.execute(new BackgroundCallback<S>() {
            @Override
            public void apply(Function<S> function) {
                Phlux.this.apply(key, function);
            }

            @Override
            public void dismiss() {
                swapScope(key, new ScopeTransaction() {
                    @Override
                    public Scope transact(Scope scope) {
                        return scope == null ? null : scope
                            .withBackground(without(scope.background, id))
                            .withCancellable(without(scope.cancellable, id));
                    }
                });
            }
        });
    }

    private interface Transaction {
        Map<String, Scope> transact(Map<String, Scope> root);
    }

    private interface ScopeTransaction {
        Scope transact(Scope scope);
    }

    static class Scope implements Parcelable {

        final ViewState state;
        final List<StateCallback> callbacks;
        final Map<Integer, Background> background;
        final Map<Integer, Cancellable> cancellable;

        Scope(ViewState state, List<StateCallback> callbacks, Map<Integer, Background> background, Map<Integer, Cancellable> cancellable) {
            this.state = state;
            this.callbacks = callbacks;
            this.background = background;
            this.cancellable = cancellable;
        }

        Scope(ViewState state) {
            this.state = state;
            this.callbacks = Collections.emptyList();
            this.background = Collections.emptyMap();
            this.cancellable = Collections.emptyMap();
        }

        public Scope withState(ViewState state) {
            return new Scope(state, callbacks, background, cancellable);
        }

        public Scope withCallbacks(List<StateCallback> callbacks) {
            return new Scope(state, callbacks, background, cancellable);
        }

        public Scope withBackground(Map<Integer, Background> background) {
            return new Scope(state, callbacks, background, cancellable);
        }

        public Scope withCancellable(Map<Integer, Cancellable> cancellable) {
            return new Scope(state, callbacks, background, cancellable);
        }

        protected Scope(Parcel in) {
            this.state = in.readParcelable(ViewState.class.getClassLoader());
            this.callbacks = Collections.emptyList();
            this.background = Collections.unmodifiableMap(in.readHashMap(ViewState.class.getClassLoader()));
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
