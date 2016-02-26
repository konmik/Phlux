package phlux;

/**
 * See {@link PhluxBackground}
 */
public interface PhluxBackgroundCallback<S extends PhluxState> {
    void apply(PhluxFunction<S> function);
    void dismiss();
}
