package phlux;

import android.os.Parcelable;

import rx.functions.Action1;

/**
 * Represents a background action.
 *
 * All subclasses of {@link PhluxBackground} *must* be immutable.
 */
public interface PhluxBackground<S extends PhluxState> extends Parcelable {
    /**
     * Executes the action.
     *
     * @param callback a method chat should be called in the action completion.
     */
    void execute(Action1<PhluxFunction<S>> callback);
}
