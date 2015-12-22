package base;

import android.app.Activity;
import android.os.Bundle;

import phlux.PhluxBackground;
import phlux.PhluxFunction;
import phlux.PhluxState;
import rx.Subscription;

/**
 * This is an *example* of how to adopt Phlux to your Activities.
 */
public abstract class PhluxActivity<S extends PhluxState> extends Activity {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private RxPhluxScope<S> scope;
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        scope = savedInstanceState == null ?
            new RxPhluxScope<>(initial()) :
            new RxPhluxScope<>(savedInstanceState.getBundle(PHLUX_SCOPE));
    }

    public void apply(PhluxFunction<S> function) {
        scope.apply(function);
    }

    public void background(int id, PhluxBackground<S> background, boolean sticky) {
        scope.background(id, background, sticky);
    }

    protected abstract S initial();
    protected abstract void update(S state);

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHLUX_SCOPE, scope.save());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (subscription == null)
            subscription = scope.state()
                .subscribe(this::update);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
        subscription = null;
        if (isFinishing())
            scope.remove();
    }
}
