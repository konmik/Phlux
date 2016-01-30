package example;

import android.support.annotation.Nullable;

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
    public abstract Transient<ServerAPI.Item[]> items();
    @Nullable
    public abstract String error();

    public abstract Builder toBuilder();

    public static MainState create() {
        return new AutoParcel_MainState.Builder()
            .name(DEFAULT_NAME)
            .items(new Transient<>())
            .error(null)
            .build();
    }

    @AutoParcel.Builder
    interface Builder {
        Builder name(String x);
        Builder items(Transient<ServerAPI.Item[]> x);
        Builder error(String x);
        MainState build();
    }
}
