package base;

import android.app.Activity;
import android.os.Bundle;

import phlux.PhluxBackground;
import phlux.PhluxFunction;
import phlux.PhluxState;
import rx.Subscription;
import rx.functions.Action1;

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
            new RxPhluxScope<S>(initial()) :
            new RxPhluxScope<S>(savedInstanceState.getBundle(PHLUX_SCOPE));
    }

    public void apply(PhluxFunction<S> action) {
        scope.apply(action);
    }

    public void background(int id, PhluxBackground<S> action, boolean sticky) {
        scope.background(id, sticky, action);
    }

    protected abstract S initial();
    protected abstract void update(S state);

    /**
     * Override this method to bind your own RxJava subscriptions.
     * The method is called during the first onResume.
     * The returned subscription will be unsubscibed during onDestroy.
     */
    protected Subscription subscribe() {
        return scope.state()
            .subscribe(new Action1<S>() {
                @Override
                public void call(S state) {
                    update(state);
                }
            });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Bundle state = scope.save();
        outState.putParcelable(PHLUX_SCOPE, state);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (subscription == null)
            subscription = subscribe();
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
