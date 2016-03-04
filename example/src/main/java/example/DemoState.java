package example;

import auto.parcel.AutoParcel;
import phlux.PhluxState;

@AutoParcel
public abstract class DemoState implements PhluxState {

    public abstract float progress();

    public static DemoState create(float progress) {
        return new AutoParcel_DemoState(progress);
    }

    public static DemoState create() {
        return new AutoParcel_DemoState(0);
    }
}
