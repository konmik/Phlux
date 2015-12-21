package phlux;

public interface PhluxBackgroundCallback<S extends PhluxState> {
    void call(PhluxFunction<S> function);
}
