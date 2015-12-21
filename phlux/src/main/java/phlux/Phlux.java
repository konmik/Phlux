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
 * Don't use the class directly - {@link PhluxScope} is a more convenient way.
 */
public enum Phlux {

    INSTANCE;

    private Map<String, ScopeData> root = Collections.emptyMap();
    private Map<String, List<PhluxStateCallback>> callbacks = Collections.emptyMap();

    public ScopeData get(String key) {
        return root.get(key);
    }

    public void put(String key, ScopeData data) {
        root = with(root, key, data);
    }

    public <S extends PhluxState> void apply(String key, PhluxFunction<S> action) {
        ScopeData data = root.get(key);
        S oldValue = (S) data.state;
        S newValue = action.call(oldValue);
        root = with(root, key, new ScopeData(newValue, data.background));

        for (PhluxStateCallback callback : callbacks.get(key))
            callback.call(newValue);
    }

    public <S extends PhluxState> void execute(final String key, final int id, final Phlux.BackgroundEntry entry) {
        entry.action.execute(new PhluxBackgroundCallback<S>() {
            @Override
            public void call(PhluxFunction<S> function) {
                ScopeData data = root.get(key);
                if (data.background.values().contains(entry)) {
                    if (!entry.sticky)
                        root = with(root, key, new ScopeData(data.state, without(data.background, id)));
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
        ScopeData data = root.get(key);
        Phlux.BackgroundEntry entry = new Phlux.BackgroundEntry(action, sticky);
        root = with(root, key, new ScopeData(data.state, with(data.background, id, entry)));
        execute(key, id, entry);
    }

    static class ScopeData implements Parcelable {

        final PhluxState state;
        final Map<Integer, BackgroundEntry> background;

        public ScopeData(PhluxState state, Map<Integer, BackgroundEntry> background) {
            this.state = state;
            this.background = background;
        }

        protected ScopeData(Parcel in) {
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

        public static final Creator<ScopeData> CREATOR = new Creator<ScopeData>() {
            @Override
            public ScopeData createFromParcel(Parcel in) {
                return new ScopeData(in);
            }

            @Override
            public ScopeData[] newArray(int size) {
                return new ScopeData[size];
            }
        };
    }

    static class BackgroundEntry implements Parcelable {

        final PhluxBackground action;
        final boolean sticky;

        public BackgroundEntry(PhluxBackground action, boolean sticky) {
            this.action = action;
            this.sticky = sticky;
        }

        protected BackgroundEntry(Parcel in) {
            action = in.readParcelable(PhluxBackground.class.getClassLoader());
            sticky = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(action, flags);
            dest.writeByte((byte) (sticky ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<BackgroundEntry> CREATOR = new Creator<BackgroundEntry>() {
            @Override
            public BackgroundEntry createFromParcel(Parcel in) {
                return new BackgroundEntry(in);
            }

            @Override
            public BackgroundEntry[] newArray(int size) {
                return new BackgroundEntry[size];
            }
        };
    }
}
