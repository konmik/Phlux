package example;

import auto.parcel.AutoParcel;
import base.App;
import phlux.Background;
import phlux.BackgroundCallback;
import phlux.Cancellable;
import phlux.Transient;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

@AutoParcel
public abstract class Request implements Background<MainState> {

    public abstract String name();

    public static Request create(String name) {
        return new AutoParcel_Request(name);
    }

    @Override
    public Cancellable execute(BackgroundCallback<MainState> callback) {
        String firstName = name().split("\\s+")[0];
        String lastName = name().split("\\s+")[1];
        return App.getServerAPI()
            .getItems(firstName, lastName)
            .observeOn(mainThread())
            .subscribe(
                response ->
                    callback.apply(state -> state.toBuilder()
                        .items(Transient.of(response.items))
                        .build()),
                throwable ->
                    callback.apply(state -> state.toBuilder()
                        .error(throwable.toString())
                        .build()))
            ::unsubscribe;
    }
}
