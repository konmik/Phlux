package phlux;

/**
 * See {@link Background}
 */
public interface BackgroundCallback<S extends ViewState> {
    void apply(Function<S> function);
    void dismiss();
}
