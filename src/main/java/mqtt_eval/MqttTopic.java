package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.rhea_core.Stream;
import org.rhea_core.internal.Notification;
import org.rhea_core.internal.output.Output;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.rhea_core.io.ExternalTopic;

import java.util.ArrayList;
import java.util.List;

public class MqttTopic extends ExternalTopic<byte[], MqttAsyncClient> {
    static final long DELAY = 250;
    static final int QOS = 2; // slowest && most reliable

    public MqttTopic(String name) {
        super(name);
        this.name = name;
    }

    @Override
    public void setClient(MqttAsyncClient client) {
        this.client = client;
    }

    /**
     * Publisher implementation.
     */
    @Override
    public void subscribe(Subscriber<? super byte[]> s) {
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                cause.printStackTrace();
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                s.onNext(message.getPayload());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {}
        });
    }

    /**
     * Subscriber implementation.
     */
    @Override
    public void onSubscribe(Subscription s) {
        s.request(1);
    }

    @Override
    public void onNext(byte[] t) {
        publish(t);
    }

    @Override
    public void onError(Throwable t) {}

    @Override
    public void onComplete() {}

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
    public MqttTopic clone() {
        return new MqttTopic(name);
    }

    public static List<MqttTopic> extract(Stream stream, Output output) {
        List<MqttTopic> topics = new ArrayList<>();

        for (ExternalTopic topic : ExternalTopic.extractAll(stream, output))
            if (topic instanceof MqttTopic)
                topics.add(((MqttTopic) topic));

        return topics;
    }
}
