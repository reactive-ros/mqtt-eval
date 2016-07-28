package mqtt_eval;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.rhea_core.Stream;
import org.rhea_core.internal.Notification;
import org.rhea_core.internal.output.Output;
import org.rhea_core.io.ExternalTopic;
import org.rhea_core.io.InternalTopic;
import org.rhea_core.serialization.DefaultSerializationStrategy;

import java.util.ArrayList;
import java.util.List;

public class MqttInternalTopic<T> extends InternalTopic<T, MqttAsyncClient, byte[]> {
    static final long DELAY = 250;
    static final int QOS = 2; // slowest && most reliable

    public MqttInternalTopic(String name) {
        super(name, new DefaultSerializationStrategy());
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
                Notification<T> not = serializationStrategy.deserialize(message.getPayload());
                switch (not.getKind()) {
                    case OnCompleted:
                        s.onComplete();
                        break;
                    case OnError:
                        s.onError(not.getThrowable());
                        break;
                    default:
                        s.onNext(not.getValue());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(T t) {
        publish(serializationStrategy.serialize(Notification.createOnNext(t)));
    }

    @Override
    public void onError(Throwable t) {
        publish(serializationStrategy.serialize(Notification.createOnError(t)));
    }

    @Override
    public void onComplete() {
        publish(serializationStrategy.serialize(Notification.createOnCompleted()));
    }

    private void publish(byte[] payload) {
        try {
            MqttMessage msg = new MqttMessage(payload);
            msg.setQos(QOS);
//            msg.setRetained(true);
            client.publish(name, msg);
        } catch (MqttException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(DELAY);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public MqttInternalTopic clone() {
        return new MqttInternalTopic(name);
    }

    public static List<MqttInternalTopic> extract(Stream stream, Output output) {
        List<MqttInternalTopic> topics = new ArrayList<>();

        for (ExternalTopic topic : ExternalTopic.extractAll(stream, output))
            if (topic instanceof MqttInternalTopic)
                topics.add(((MqttInternalTopic) topic));

        return topics;
    }
}
