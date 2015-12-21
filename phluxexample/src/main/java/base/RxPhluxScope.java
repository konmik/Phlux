package base;

import android.os.Bundle;

import phlux.PhluxStateCallback;
import phlux.PhluxScope;
import phlux.PhluxState;
import rx.Observable;
import rx.subscriptions.Subscriptions;

public class RxPhluxScope<S extends PhluxState> extends PhluxScope<S> {

    public RxPhluxScope(S state) {
        super(state);
    }

    public RxPhluxScope(Bundle bundle) {
        super(bundle);
    }

    public Observable<S> state() {
        return Observable.<S>create(subscriber -> {
            PhluxStateCallback<S> callback = subscriber::onNext;
            register(callback);
            subscriber.add(Subscriptions.create(() -> unregister(callback)));
        });
    }
}
