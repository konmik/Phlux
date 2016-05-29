package phlux;

/**
 * Represents a callback that should be called on each {@link ViewState} update.
 */
public interface StateCallback<S extends ViewState> {
    void call(S state);
}
