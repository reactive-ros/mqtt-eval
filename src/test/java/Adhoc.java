import mqtt_eval.MqttExample;
import org.junit.Test;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * @author Orestis Melkonian
 */
public class Adhoc {

    @Test
    public void mqtt() {
        String topic = "MQTT Examples";
        String content = "Message from MqttPublishSample";
        int qos = 2;
        String broker = "tcp://iot.eclipse.org:1883";
        String clientId = "JavaSample";
        MemoryPersistence persistence = new MemoryPersistence();

        MqttClient client = null;
        try {
            client = new MqttClient(broker, clientId, persistence);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        /*System.out.println("Connecting to broker: "+broker);
        sampleClient.connect(connOpts);
        System.out.println("Connected");
        System.out.println("Publishing message: "+content);
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        sampleClient.publish(topic, message);
        System.out.println("Message published");
        sampleClient.disconnect();
        System.out.println("Disconnected");
        System.exit(0);*/


        new MqttExample().doDemo(client);


        try {
            Thread.sleep(Long.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
