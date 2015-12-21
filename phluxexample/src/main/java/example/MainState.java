package example;

import android.os.Parcelable;

import auto.parcel.AutoParcel;
import base.ServerAPI;
import phlux.PhluxState;
import phlux.Transient;

@AutoParcel
public abstract class MainState implements Parcelable, PhluxState {
    public abstract String name();
    public abstract Transient<String> error();
    public abstract Transient<ServerAPI.Item[]> items();

    public abstract Builder toBuilder();

    public static MainState create(String name) {
        return new AutoParcel_MainState.Builder()
            .name(name)
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
