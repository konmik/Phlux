package base;

import android.app.Activity;
import android.os.Bundle;

import phlux.PhluxBackground;
import phlux.PhluxFunction;
import phlux.PhluxScope;
import phlux.PhluxState;
import phlux.PhluxView;
import phlux.PhluxViewAdapter;

/**
 * This is an *example* of how to adapt Phlux to Activities.
 */
public abstract class PhluxActivity<S extends PhluxState> extends Activity implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private PhluxViewAdapter<S> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PhluxViewAdapter<>(
            savedInstanceState == null ?
                new PhluxScope<>(initial()) :
                new PhluxScope<>(savedInstanceState.getBundle(PHLUX_SCOPE)),
            this::update);
    }

    public void post(Runnable runnable) {
        getWindow()
            .getDecorView()
            .post(runnable);
    }

    protected abstract S initial();

    protected abstract void update(S state);

    @Override
    public void apply(PhluxFunction<S> function) {
        adapter.scope().apply(function);
    }

    @Override
    public void background(int id, PhluxBackground<S> background, boolean sticky) {
        adapter.scope().background(id, background, sticky);
    }

    public void setUpdateAllOnResume(boolean update) {
        adapter.setUpdateAllOnResume(update);
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
