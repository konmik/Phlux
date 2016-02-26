package phlux;

public interface PhluxBackgroundCancellable {

    PhluxBackgroundCancellable NOOP = new PhluxBackgroundCancellable() {
        @Override
        public void cancel() {
        }
    };

    void cancel();
}
