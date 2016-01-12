package phlux;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * An utility class that represents a reference to a value that should not be parceled.
 */
public class Transient<T> implements Parcelable {

    private static final Transient EMPTY = new Transient();

    public final T value;

    public Transient() {
        value = null;
    }

    public T get() {
        return value;
    }

    public boolean isPresent() {
        return value != null;
    }

    public static <T> Transient<T> empty() {
        return EMPTY;
    }

    public static <T> Transient<T> of(T value) {
        return value == null ? EMPTY : new Transient<>(value);
    }

    protected Transient(Parcel ignored, boolean fromParcel) {
        this.value = null;
    }

    private Transient(T value) {
        this.value = value;
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
