package phlux.base;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import phlux.PhluxBackground;
import phlux.PhluxFunction;
import phlux.PhluxScope;
import phlux.PhluxState;
import phlux.PhluxStateCallback;
import phlux.PhluxViewAdapter;

/**
 * This is an *example* of how to adapt Phlux to Views.
 */
public abstract class PhluxView<S extends PhluxState> extends FrameLayout implements phlux.PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private static final String SUPER = "super";

    private PhluxViewAdapter<S> adapter;
    private PhluxStateCallback<S> phluxStateCallback = new PhluxStateCallback<S>() {
        @Override
        public void call(S state) {
            update(state);
        }
    };

    public PhluxView(Context context) {
        super(context);
    }

    public PhluxView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PhluxView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        if (isInEditMode())
            return;

        if (adapter == null)
            adapter = new PhluxViewAdapter<>(new PhluxScope<>(initial()), phluxStateCallback);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (getActivity().isFinishing()) {
            adapter.scope().remove();
        }
        adapter.onDestroy();
        adapter = null;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable(SUPER));
        adapter = new PhluxViewAdapter<>(new PhluxScope<S>(bundle.getBundle(PHLUX_SCOPE)), phluxStateCallback);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(PHLUX_SCOPE, adapter.scope().save());
        bundle.putParcelable(SUPER, super.onSaveInstanceState());
        return bundle;
    }

    public Activity getActivity() {
        Context context = getContext();
        while (!(context instanceof Activity) && context instanceof ContextWrapper)
            context = ((ContextWrapper) context).getBaseContext();
        if (!(context instanceof Activity))
            throw new IllegalStateException("Expected an activity context, got " + context.getClass().getSimpleName());
        return (Activity) context;
    }

    @Override
    public void apply(PhluxFunction<S> function) {
        adapter.scope().apply(function);
    }

    @Override
    public void background(int id, PhluxBackground<S> background) {
        adapter.scope().background(id, background);
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
}
