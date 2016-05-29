package phlux;

import android.os.Bundle;

import java.util.HashMap;

/**
 * PhluxViewAdapter incorporates common view logic.
 * This should be a single place where view state is stored.
 */
public class PhluxViewAdapter<S extends ViewState> {

    private final PhluxView<S> view;
    private final StateCallback<S> callback = new StateCallback<S>() {
        @Override
        public void call(S state) {
            view.update(state);
        }
    };
    private Scope<S> scope;

    private HashMap<String, Object> updated = new HashMap<>();

    public PhluxViewAdapter(PhluxView<S> view) {
        this.view = view;
    }

    public void onRestore(Bundle bundle) {
        if (scope != null)
            throw new IllegalStateException("onRestore() must be called before scope() and before onResume()");

        scope = new Scope<>(bundle);
        scope.register(callback);
    }

    public Scope<S> scope() {
        if (scope == null) {
            scope = new Scope<>(view.initial());
            scope.register(callback);
            view.onScopeCreated(scope);
        }
        return scope;
    }

    public <T> void part(String name, T newValue, PhluxView.FieldUpdater<T> updater) {
        if (!updated.containsKey(name) || updated.get(name) != newValue) {
            updater.call(newValue);
            updated.put(name, newValue);
        }
    }

    public void resetParts() {
        updated.clear();
    }

    public void onResume() {
        view.update(scope().state());
    }

    public void onDestroy() {
        scope.unregister(callback);
    }
}
