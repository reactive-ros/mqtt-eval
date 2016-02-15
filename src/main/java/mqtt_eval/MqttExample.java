package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;

/**
 * @author Orestis Melkonian
 */
public class MqttExample implements MqttCallback {


    public void doDemo(MqttClient client) {
        try {
            client.connect();
            client.setCallback(this);
            client.subscribe("foo");
            MqttMessage message = new MqttMessage();
            message.setPayload("A single message from my computer fff".getBytes());
            client.publish("foo", message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        System.out.println("[" + topic + "]: " + message);
    }

    @Override
    public void connectionLost(Throwable cause) {
        cause.printStackTrace();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
    }
}
