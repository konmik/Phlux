package phlux;

import android.os.Parcelable;

/**
 * This is a base class for activity/fragment state.
 *
 * An activity/fragment normally should have one {@link PhluxState} which contains
 * all data that is used to display the activity/fragment.
 *
 * All {@link PhluxState} subclasses *must* be immutable.
 */
public interface PhluxState extends Parcelable {
}
