package example;

import org.junit.Test;

import rx.Observable;
import rx.Subscriber;

import static org.junit.Assert.*;

public class MainActivityTest {

    @Test
    public void testShare() throws Exception {
        Observable.create(new Observable.OnSubscribe<Object>() {
            @Override
            public void call(Subscriber<? super Object> subscriber) {

            }
        }).share();
    }
}