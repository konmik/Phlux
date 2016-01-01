package example;

import auto.parcel.AutoParcel;
import base.App;
import phlux.PhluxBackground;
import phlux.PhluxBackgroundCallback;
import phlux.Transient;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

@AutoParcel
public abstract class Request implements PhluxBackground<MainState> {

    public abstract String name();

    public static Request create(String name) {
        return new AutoParcel_Request(name);
    }

    @Override
    public void execute(PhluxBackgroundCallback<MainState> callback) {
        App.getServerAPI()
            .getItems(name().split("\\s+")[0], name().split("\\s+")[1])
            .observeOn(mainThread())
            .subscribe(
                response ->
                    callback.call(state -> state.toBuilder()
                        .items(new Transient<>(response.items))
                        .build()),
                throwable ->
                    callback.call(state -> state.toBuilder()
                        .error(new Transient<>(throwable.toString()))
                        .build()));
    }
}
