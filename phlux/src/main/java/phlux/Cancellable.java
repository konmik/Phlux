package phlux;

public interface Cancellable {

    Cancellable NOOP = new Cancellable() {
        @Override
        public void cancel() {
        }
    };

    void cancel();
}
