package example;

import android.os.AsyncTask;

import auto.parcel.AutoParcel;
import phlux.PhluxBackground;
import phlux.PhluxBackgroundCallback;
import phlux.PhluxBackgroundCancellable;

@AutoParcel
public abstract class DemoTask1 implements PhluxBackground<DemoState> {

    public static DemoTask1 create() {
        return new AutoParcel_DemoTask1();
    }

    @Override
    public PhluxBackgroundCancellable execute(PhluxBackgroundCallback<DemoState> callback) {
        AsyncTask<Void, Float, Void> task = new AsyncTask<Void, Float, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (float progress = 0; progress <= 100 && !isCancelled(); progress++) {
                    publishProgress(progress);
                    sleep();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Float... values) {
                super.onProgressUpdate(values);
                if (values.length > 0)
                    callback.apply(state -> DemoState.create(values[values.length - 1]));
            }

            private void sleep() {
                try {
                    Thread.sleep(100);
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.execute();
        return () -> task.cancel(true);
    }
}
