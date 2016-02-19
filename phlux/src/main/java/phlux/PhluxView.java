package phlux;

import android.os.Bundle;
import android.view.View;

/**
 * This is the recommended interface for phlux enabled views.
 */
public interface PhluxView<S extends PhluxState> {

    interface FieldUpdater<T> {
        void call(T value);
    }

    S state();
    void apply(PhluxFunction<S> function);
    void background(int taskId, PhluxBackground<S> background);
    void drop(int taskId);

    /**
     * Incorporates updating logic for a visual part of an activity,
     * allowing to update only parts of the activity that have updated state.
     * <p>
     * Given we have a deal with immutable state, we only compare
     * references to values instead of calling equals() to detect
     * changed data.
     */
    <T> void part(String name, T newValue, FieldUpdater<T> updater);

    /**
     * Resets "updated" flag for all parts.
     * First successive {@link #part(String, Object, FieldUpdater)} calls for each part
     * will cause updater to be fired.
     * <p>
     * Call this method when view has been recreated, for example during {@link android.app.Fragment#onViewCreated(View, Bundle)}.
     */
    void resetParts();

    /**
     * onStateCreated is called when state was created for the first time.
     * This is a good place to initialize background tasks.
     */
    void onStateCreated(S state);

    /**
     * Creates an initial state.
     * Must be implemented by a specific View implementation.
     */
    S initial();

    /**
     * Updated view from a given state.
     * Must be implemented by a specific View implementation.
     */
    void update(S state);
}
