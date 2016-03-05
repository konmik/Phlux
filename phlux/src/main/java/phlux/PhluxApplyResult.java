package phlux;

import android.support.annotation.Nullable;

public class PhluxApplyResult<T> {

    @Nullable public final T prev;
    @Nullable public final T now;

    public PhluxApplyResult(@Nullable T prev, @Nullable T now) {
        this.prev = prev;
        this.now = now;
    }
}
