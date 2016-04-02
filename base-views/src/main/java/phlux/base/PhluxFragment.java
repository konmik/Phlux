package phlux.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import phlux.PhluxScope;
import phlux.PhluxState;
import phlux.PhluxView;
import phlux.PhluxViewAdapter;

/**
 * This is an *example* of how to adapt Phlux to Activities.
 */
public abstract class PhluxFragment<S extends PhluxState> extends Fragment implements PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";

    private final PhluxViewAdapter<S> adapter = new PhluxViewAdapter<>(this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            adapter.onRestore(savedInstanceState.getBundle(PHLUX_SCOPE));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        adapter.resetParts();
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
    public void onScopeCreated(PhluxScope<S> scope) {
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
        if (getActivity().isFinishing() || isRemoving(this))
            adapter.scope().remove();
    }

    private static boolean isRemoving(Fragment fragment) {
        Fragment parent = fragment.getParentFragment();
        return fragment.isRemoving() || (parent != null && isRemoving(parent));
    }
}
