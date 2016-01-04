package example;

import auto.parcel.AutoParcel;
import base.ServerAPI;
import phlux.PhluxState;
import phlux.Transient;

@AutoParcel
public abstract class MainState implements PhluxState {

    static final String NAME_1 = "Chuck Norris";
    static final String NAME_2 = "Jackie Chan";
    static final String DEFAULT_NAME = NAME_1;

    public abstract String name();
    public abstract Transient<String> error();
    public abstract Transient<ServerAPI.Item[]> items();

    public abstract Builder toBuilder();

    public static MainState create() {
        return new AutoParcel_MainState.Builder()
            .name(DEFAULT_NAME)
            .error(new Transient<>())
            .items(new Transient<>())
            .build();
    }

    @AutoParcel.Builder
    interface Builder {
        Builder name(String x);
        Builder error(Transient<String> x);
        Builder items(Transient<ServerAPI.Item[]> x);
        MainState build();
    }
}
