package phlux;

import android.os.Parcelable;

/**
 * This is a base class for state.
 *
 * For example, a Fragment normally have one {@link PhluxState} which contains
 * all data that is used to display the Fragment.
 *
 * All {@link PhluxState} subclasses *must* be immutable.
 */
public interface PhluxState extends Parcelable {
}
