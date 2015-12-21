package phlux;

import android.os.Parcelable;

import rx.functions.Action1;

/**
 * Represents a background action.
 *
 * The typical use case is to extend the class to keep arguments in it and to implement
 * {@link PhluxBackground#execute(Action1)} to execute the background task itself.
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
    void execute(Action1<PhluxFunction<S>> callback);
}
