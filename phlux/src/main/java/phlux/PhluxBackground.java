package phlux;

import android.os.Parcelable;

/**
 * Represents a background action.
 *
 * The typical use case is to extend the class to keep arguments in it and to implement
 * {@link PhluxBackground#execute(PhluxBackgroundCallback)} to execute the background task itself.
 *
 * All subclasses of {@link PhluxBackground} *must* be immutable.
 */
public interface PhluxBackground<S extends PhluxState> extends Parcelable {
    /**
     * Executes the action.
     *
     * @param callback a method that should be called on the action completion. It must provide
     *                 a function that must be applied to state.
     *                 The current implementation implies that the callback must be called
     *                 on the main thread.
     */
    void execute(PhluxBackgroundCallback<S> callback);
}
