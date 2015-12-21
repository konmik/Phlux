package phlux;

/**
 * See {@link PhluxBackground}
 */
public interface PhluxBackgroundCallback<S extends PhluxState> {
    void call(PhluxFunction<S> function);
}
