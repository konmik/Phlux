package phlux;

import android.os.Parcel;
import android.os.Parcelable;

class TestState implements ViewState, Parcelable {

    public TestState() {
    }

    protected TestState(Parcel in) {
    }

    public static final Creator<TestState> CREATOR = new Creator<TestState>() {
        @Override
        public TestState createFromParcel(Parcel in) {
            return new TestState(in);
        }

        @Override
        public TestState[] newArray(int size) {
            return new TestState[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
