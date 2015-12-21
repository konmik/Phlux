package phlux;

/**
 * This is a single function interface that is used to
 * create a modified version of a given {@link PhluxState}.
 */
public interface PhluxFunction<S extends PhluxState> {
    S call(S state);
}
