package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.rhea_core.evaluation.GeneralSerializer;
import org.rhea_core.internal.notifications.Notification;
import org.rhea_core.io.AbstractTopic;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class MqttTopic<T> extends AbstractTopic<T, byte[], MqttAsyncClient> {
    static final boolean DEBUG = false;
    static final int QOS = 2; // slowest && most reliable

    public MqttTopic(String name) {
        super(name, new GeneralSerializer());
        this.name = name;
    }

    @Override
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

    @Override
    public MqttTopic clone() {
        return new MqttTopic(name);
    }
}
