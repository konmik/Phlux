package phlux.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import phlux.PhluxBackground;
import phlux.PhluxFunction;
import phlux.PhluxScope;
import phlux.PhluxState;
import phlux.PhluxStateCallback;
import phlux.PhluxView;
import phlux.PhluxViewAdapter;

/**
 * This is an *example* of how to adapt Phlux to Activities.
 */
public abstract class PhluxActivity<S extends PhluxState> extends AppCompatActivity implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";

    private PhluxViewAdapter<S> adapter;
    private PhluxStateCallback<S> phluxStateCallback = new PhluxStateCallback<S>() {
        @Override
        public void call(S state) {
            update(state);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        adapter = new PhluxViewAdapter<>(
            savedInstanceState == null ?
                new PhluxScope<>(initial()) :
                new PhluxScope<S>(savedInstanceState.getBundle(PHLUX_SCOPE)),
            phluxStateCallback);
    }

    public void post(Runnable runnable) {
        getWindow()
            .getDecorView()
            .post(runnable);
    }

    @Override
    public void apply(PhluxFunction<S> function) {
        adapter.scope().apply(function);
    }

    @Override
    public void background(int taskId, PhluxBackground<S> background, boolean sticky) {
        adapter.scope().background(taskId, background, sticky);
    }

    @Override
    public void drop(int taskId) {
        adapter.scope().drop(taskId);
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
    public void resetParts() {
        adapter.resetParts();
    }

    @Override
    public void onStateCreated(S state) {
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
