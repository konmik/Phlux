package phlux.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

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
public abstract class PhluxFragment<S extends PhluxState> extends Fragment implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";

    private PhluxViewAdapter<S> adapter;
    private PhluxStateCallback<S> phluxStateCallback = new PhluxStateCallback<S>() {
        @Override
        public void call(S state) {
            update(state);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            adapter = new PhluxViewAdapter<>(new PhluxScope<S>(savedInstanceState.getBundle(PHLUX_SCOPE)), phluxStateCallback);
        else {
            adapter = new PhluxViewAdapter<>(new PhluxScope<>(initial()), phluxStateCallback);
            onStateCreated(adapter.scope().state());
        }
    }

    @Override
    public void onStateCreated(S state) {
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.resetParts();
    }

    @Override
    public void apply(PhluxFunction<S> function) {
        adapter.scope().apply(function);
    }

    @Override
    public void background(int id, PhluxBackground<S> background, boolean sticky) {
        adapter.scope().background(id, background, sticky);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(PHLUX_SCOPE, adapter.scope().save());
    }

    @Override
    public void onResume() {
        super.onResume();
        adapter.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        adapter.onDestroy();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (getActivity().isFinishing())
            adapter.scope().remove();
    }
}
