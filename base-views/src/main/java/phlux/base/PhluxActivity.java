package phlux.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import phlux.PhluxScope;
import phlux.PhluxState;
import phlux.PhluxView;
import phlux.PhluxViewAdapter;

/**
 * This is an *example* of how to adapt Phlux to Activities.
 */
public abstract class PhluxActivity<S extends PhluxState> extends AppCompatActivity implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";

    private final PhluxViewAdapter<S> adapter = new PhluxViewAdapter<>(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            adapter.onRestore(savedInstanceState.getBundle(PHLUX_SCOPE));
    }

    public void post(Runnable runnable) {
        getWindow()
            .getDecorView()
            .post(runnable);
    }

    @Override
    public PhluxScope<S> scope() {
        return adapter.scope();
    }

    @Override
    public S state() {
        return adapter.scope().state();
    }

    @Override
    public <T> void part(String name, T newValue, FieldUpdater<T> updater) {
        adapter.part(name, newValue, updater);
    }

    @Override
    public void resetParts() {
        adapter.resetParts();
    }

    @Override
    public void onScopeCreated(PhluxScope<S> state) {
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHLUX_SCOPE, adapter.scope().save());
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        adapter.onDestroy();
        if (isFinishing())
            adapter.scope().remove();
    }
}
