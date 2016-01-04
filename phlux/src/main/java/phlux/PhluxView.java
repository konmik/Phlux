package phlux;

/**
 * This is the recommended interface for phlux enabled views.
 */
public interface PhluxView<S extends PhluxState> {

    interface FieldUpdater<T> {
        void call(T value);
    }

    S state();
    void apply(PhluxFunction<S> function);
    void background(int id, PhluxBackground<S> background, boolean sticky);

    /**
     * Incorporates updating logic for a visual part of an activity,
     * allowing to update only parts of the activity that have updated state.
     * <p>
     * Given we have a deal with immutable state, we only compare
     * references to objects instead of calling equals() to detect
     * changed data.
     */
    <T> void part(String name, T newValue, FieldUpdater<T> updater);
}
