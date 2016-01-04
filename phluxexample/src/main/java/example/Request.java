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
        String firstName = name().split("\\s+")[0];
        String lastName = name().split("\\s+")[1];
        App.getServerAPI()
            .getItems(firstName, lastName)
            .observeOn(mainThread())
            .subscribe(
                response ->
                    callback.call(state -> state.toBuilder()
                        .items(new Transient<>(response.items))
                        .build()),
                throwable ->
                    callback.call(state -> state.toBuilder()
                        .error(throwable.toString())
                        .build()));
    }
}
