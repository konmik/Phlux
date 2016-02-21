package phlux;

public interface PhluxBackgroundCancellable {

    PhluxBackgroundCancellable EMPTY = new PhluxBackgroundCancellable() {
        @Override
        public void cancel() {
        }
    };

    void cancel();
}
