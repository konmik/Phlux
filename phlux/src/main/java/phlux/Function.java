package phlux;

/**
 * This is a function interface that is used to
 * create a modified version of a given {@link ViewState}.
 */
public interface Function<S extends ViewState> {
    S call(S state);
}
