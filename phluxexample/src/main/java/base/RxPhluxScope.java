package base;

import android.os.Bundle;

import phlux.PhluxCallback;
import phlux.PhluxScope;
import phlux.PhluxState;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

public class RxPhluxScope<S extends PhluxState> extends PhluxScope<S> {

    public RxPhluxScope(S state) {
        super(state);
    }

    public RxPhluxScope(Bundle bundle) {
        super(bundle);
    }

    public Observable<S> state() {
        return Observable.create(new Observable.OnSubscribe<S>() {
            @Override
            public void call(final Subscriber<? super S> subscriber) {
                final PhluxCallback<S> callback = new PhluxCallback<S>() {
                    @Override
                    public void call(S state) {
                        subscriber.onNext(state);
                    }
                };
                register(callback);
                subscriber.add(Subscriptions.create(new Action0() {
                    @Override
                    public void call() {
                        unregister(callback);
                    }
                }));
            }
        });
    }
}
