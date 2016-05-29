package phlux.base;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import phlux.Scope;
import phlux.ViewState;
import phlux.PhluxViewAdapter;

/**
 * This is an *example* of how to adapt Phlux to Views.
 */
public abstract class PhluxView<S extends ViewState> extends FrameLayout implements phlux.PhluxView<S> {

    private static final String PHLUX_SCOPE = "phlux_scope";
    private static final String SUPER = "super";

    private final PhluxViewAdapter<S> adapter = new PhluxViewAdapter<>(this);

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

        adapter.onResume();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (!getActivity().isChangingConfigurations())
            adapter.onDestroy();
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        super.onRestoreInstanceState(bundle.getParcelable(SUPER));
        adapter.onRestore(bundle.getBundle(PHLUX_SCOPE));
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
    public Scope<S> scope() {
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
    public void onScopeCreated(Scope<S> scope) {
    }
}
