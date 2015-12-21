package phlux;

import android.os.Parcelable;

import java.util.Collections;
import java.util.Map;

import auto.parcel.AutoParcel;
import rx.Observable;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import static phlux.Fn.with;
import static phlux.Fn.without;

/**
 * This singleton should be a single place where mutable state of the entire application is stored.
 * Don't use the class directly - {@link PhluxScope} is a more convenient way.
 * TODO: multithreading support?
 */
public enum Phlux {

    INSTANCE;

    private Map<String, ScopeData> root = Collections.emptyMap();
    private Map<String, BehaviorSubject> callbacks = Collections.emptyMap();

    public ScopeData get(String key) {
        return root.get(key);
    }

    public void put(String key, ScopeData data) {
        root = with(root, key, data);
    }

    public <T extends PhluxState> void apply(String key, PhluxFunction<T> action) {
        ScopeData data = root.get(key);
        T oldValue = (T) data.state();
        T newValue = action.call(oldValue);
        root = with(root, key, ScopeData.create(newValue, data.background()));

        BehaviorSubject<T> subject = callbacks.get(key);
        if (subject != null)
            subject.onNext(newValue);
    }

    public <T extends PhluxState> void execute(final String key, final int id, final Phlux.BackgroundEntry entry) {
        entry.action().execute(new Action1<PhluxFunction<T>>() {
            @Override
            public void call(PhluxFunction<T> action1) {
                Phlux.ScopeData data = root.get(key);
                if (data.background().values().contains(entry)) {
                    if (!entry.sticky())
                        root = with(root, key, Phlux.ScopeData.create(data.state(), without(data.background(), id)));
                    apply(key, action1);
                }
            }
        });
    }

    public void remove(String key) {
        root = without(root, key);
        callbacks = without(callbacks, key);
    }

    public <T extends PhluxState> Observable<T> state(String key) {
        if (!callbacks.containsKey(key)) {
            PhluxState initial = root.get(key).state();
            callbacks = with(callbacks, key, BehaviorSubject.create(initial));
        }
        return callbacks.get(key);
    }

    public <T extends PhluxState> void background(String key, int id, PhluxBackground<T> action, boolean sticky) {
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
