package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.reactive_ros.evaluation.Serializer;
import org.reactive_ros.GeneralSerializer;
import org.reactive_ros.internal.io.Sink;
import org.reactive_ros.internal.io.Source;
import org.reactive_ros.internal.notifications.Notification;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class Topic<T> implements Source<T>, Sink<T> {
    static final boolean DEBUG = false;
    static final int QOS = 2; // slowest && most reliable
    public String name;
    private final Serializer<byte[]> serializer = new GeneralSerializer();
    private MqttAsyncClient client;

    public Topic(String name) {
        this.name = name;
    }

    public void setClient(MqttAsyncClient client) {
        this.client = client;
    }

    @Override
    public void subscribe(Subscriber<? super T> s) {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                byte[] msg = message.getPayload();
                Notification<T> notification = serializer.deserialize(msg);
                switch (notification.getKind()) {
                    case OnNext:
                        if (DEBUG)
                            System.out.println(name() + ": Recv\t" + notification.getValue());
                        s.onNext(notification.getValue());
                        break;
                    case OnError:
                        s.onError(notification.getThrowable());
                        break;
                    case OnCompleted:
                        if (DEBUG)
                            System.out.println(name() + ": Recv\tComplete");
                        s.onComplete();
                        break;
                    default:
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
            }
        });
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(T t) {
        Notification<T> notification = Notification.createOnNext(t);
        if (DEBUG) System.out.println(name() + ": Send\t" + notification.getValue());
        publish(client, notification);
    }

    @Override
    public void onError(Throwable t) {
        publish(client, Notification.createOnError(t));
    }

    @Override
    public void onComplete() {
        if (DEBUG) System.out.println(name() + ": Send\tComplete");
        publish(client, Notification.createOnCompleted());
    }

    private void publish(MqttAsyncClient client, Notification not) {
        try {
            MqttMessage msg = new MqttMessage(serializer.serialize(not));
            msg.setQos(QOS);
//            msg.setRetained(true);
            client.publish(name, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private String name() {
        return name + "[" + Thread.currentThread().getId() + "]";
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Topic clone() {
        return new Topic(name);
    }
}
