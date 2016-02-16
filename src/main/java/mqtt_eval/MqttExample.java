package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;

/**
 * @author Orestis Melkonian
 */
public class MqttExample implements MqttCallback {


    public void doDemo(MqttClient client) {
        try {
            String topic = "MQTT Examples";
            String content = "Message from MqttPublishSample";
            int qos = 2;

            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            client.setCallback(this);
            client.subscribe(topic);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            client.publish(topic, message);
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
