package phlux;

public interface PhluxCallback<S extends PhluxState> {
    void call(S state);
}
