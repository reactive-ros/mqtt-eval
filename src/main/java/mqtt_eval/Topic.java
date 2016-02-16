package mqtt_eval;

import org.reactive_ros.evaluation.Serializer;
import org.reactive_ros.GeneralSerializer;
import org.reactive_ros.internal.io.Sink;
import org.reactive_ros.internal.io.Source;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class Topic<T> implements Source<T>, Sink<T> {
    private String name;
    private final Serializer<byte[]> serializer = new GeneralSerializer();

    public Topic(String name) {
        this.name = name;
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {

    }

    @Override
    public void onSubscribe(Subscription s) {

    }

    @Override
    public void onNext(T t) {

    }

    @Override
    public void onError(Throwable t) {

    }

    @Override
    public void onComplete() {

    }
}
