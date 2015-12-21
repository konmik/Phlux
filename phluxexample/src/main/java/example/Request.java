package example;

import auto.parcel.AutoParcel;
import base.App;
import base.ServerAPI;
import phlux.PhluxBackground;
import phlux.PhluxBackgroundCallback;
import phlux.PhluxFunction;
import phlux.Transient;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

@AutoParcel
public abstract class Request implements PhluxBackground<MainState> {

    public abstract String name();

    public static Request create(String name) {
        return new AutoParcel_Request(name);
    }

    @Override
    public void execute(final PhluxBackgroundCallback<MainState> callback) {
        App.getServerAPI()
            .getItems(name().split("\\s+")[0], name().split("\\s+")[1])
            .observeOn(mainThread())
            .subscribe(new Action1<ServerAPI.Response>() {
                @Override
                public void call(final ServerAPI.Response response) {
                    callback.call(new PhluxFunction<MainState>() {
                        @Override
                        public MainState call(MainState state) {
                            return state.toBuilder()
                                .name(name())
                                .items(new Transient<>(response.items))
                                .error(new Transient<String>())
                                .build();
                        }
                    });
                }
            }, new Action1<Throwable>() {
                @Override
                public void call(final Throwable throwable) {
                    callback.call(new PhluxFunction<MainState>() {
                        @Override
                        public MainState call(MainState state) {
                            return state.toBuilder()
                                .name(name())
                                .items(new Transient<ServerAPI.Item[]>())
                                .error(new Transient<>(throwable.toString()))
                                .build();
                        }
                    });
                }
            });
    }
}
