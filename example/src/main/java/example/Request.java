package example;

import auto.parcel.AutoParcel;
import base.App;
import phlux.PhluxBackground;
import phlux.PhluxBackgroundCallback;
import phlux.PhluxBackgroundCancellable;
import phlux.PhluxBackgroundDismiss;
import phlux.Transient;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

@AutoParcel
public abstract class Request implements PhluxBackground<MainState> {

    public abstract String name();

    public static Request create(String name) {
        return new AutoParcel_Request(name);
    }

    @Override
    public PhluxBackgroundCancellable execute(PhluxBackgroundCallback<MainState> callback, PhluxBackgroundDismiss dismiss) {
        String firstName = name().split("\\s+")[0];
        String lastName = name().split("\\s+")[1];
        return App.getServerAPI()
            .getItems(firstName, lastName)
            .observeOn(mainThread())
            .subscribe(
                response ->
                    callback.call(state -> state.toBuilder()
                        .items(Transient.of(response.items))
                        .build()),
                throwable ->
                    callback.call(state -> state.toBuilder()
                        .error(throwable.toString())
                        .build()))
            ::unsubscribe;
    }
}
