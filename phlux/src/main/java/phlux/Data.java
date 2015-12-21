package phlux;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collections;
import java.util.Map;

class Data {

    static class ScopeData implements Parcelable {

        final PhluxState state;
        final Map<Integer, BackgroundEntry> background;

        ScopeData(PhluxState state, Map<Integer, BackgroundEntry> background) {
            this.state = state;
            this.background = background;
        }

        protected ScopeData(Parcel in) {
            state = in.readParcelable(PhluxState.class.getClassLoader());
            background = Collections.unmodifiableMap(in.readHashMap(PhluxState.class.getClassLoader()));
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(state, flags);
            dest.writeMap(background);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ScopeData> CREATOR = new Creator<ScopeData>() {
            @Override
            public ScopeData createFromParcel(Parcel in) {
                return new ScopeData(in);
            }

            @Override
            public ScopeData[] newArray(int size) {
                return new ScopeData[size];
            }
        };

        @Override
        public String toString() {
            return "ScopeData{" +
                "state=" + state +
                ", background=" + background +
                '}';
        }
    }

    static class BackgroundEntry implements Parcelable {

        final PhluxBackground action;
        final boolean sticky;

        BackgroundEntry(PhluxBackground action, boolean sticky) {
            this.action = action;
            this.sticky = sticky;
        }

        protected BackgroundEntry(Parcel in) {
            action = in.readParcelable(PhluxBackground.class.getClassLoader());
            sticky = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(action, flags);
            dest.writeByte((byte) (sticky ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<BackgroundEntry> CREATOR = new Creator<BackgroundEntry>() {
            @Override
            public BackgroundEntry createFromParcel(Parcel in) {
                return new BackgroundEntry(in);
            }

            @Override
            public BackgroundEntry[] newArray(int size) {
                return new BackgroundEntry[size];
            }
        };

        @Override
        public String toString() {
            return "BackgroundEntry{" +
                "action=" + action +
                ", sticky=" + sticky +
                '}';
        }
    }
}
