package phlux;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * An utility class to represent a value that should not be parceled.
 */
public class Transient<T> implements Parcelable {

    public final T value;

    public Transient() {
        value = null;
    }

    public Transient(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    protected Transient(Parcel ignored, boolean fromParcel) {
        this.value = null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Transient> CREATOR = new Creator<Transient>() {
        @Override
        public Transient createFromParcel(Parcel in) {
            return new Transient(in, true);
        }

        @Override
        public Transient[] newArray(int size) {
            return new Transient[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Transient<?> that = (Transient<?>) o;

        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Transient{" +
            "value=" + value +
            '}';
    }
}
