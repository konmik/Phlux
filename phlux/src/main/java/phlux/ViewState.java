package phlux;

import android.os.Parcelable;

/**
 * This is a base class for activity/fragment state.
 *
 * An activity/fragment normally should have one {@link ViewState} which contains
 * all data that is used to display the activity/fragment.
 *
 * All {@link ViewState} subclasses *must* be immutable.
 */
public interface ViewState extends Parcelable {
}
