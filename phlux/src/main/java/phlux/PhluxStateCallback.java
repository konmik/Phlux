package phlux;

/**
 * Represents a callback that should be called on each {@link PhluxState} update.
 */
public interface PhluxStateCallback<S extends PhluxState> {
    void call(S state);
}
