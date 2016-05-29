package phlux;

import android.os.Parcelable;

/**
 * Represents a background task.
 *
 * The typical use case is to extend this class to keep arguments in it and to implement
 * {@link Background#execute(BackgroundCallback)} to execute the background task itself.
 *
 * All subclasses of {@link Background} *must* be immutable.
 */
public interface Background<S extends ViewState> extends Parcelable {
    /**
     * Executes the background task.
     *
     * @param callback a method that should be called on the background task completion.
     *                 It must provide a function that will be applied to state.
     *                 The current implementation implies that the callback must be called
     *                 on the main thread.
     */
    Cancellable execute(BackgroundCallback<S> callback);
}
