package phlux;

import android.support.annotation.Nullable;

public class ApplyResult<T> {

    @Nullable public final T prev;
    @Nullable public final T now;

    public ApplyResult(@Nullable T prev, @Nullable T now) {
        this.prev = prev;
        this.now = now;
    }
}
