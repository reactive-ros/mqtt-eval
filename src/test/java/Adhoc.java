
import mqtt_eval.MqttRunnable;
import mqtt_eval.Topic;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.junit.Test;
import org.reactive_ros.Stream;
import org.reactive_ros.internal.output.ActionOutput;
import org.reactive_ros.internal.output.SinkOutput;
import rx_eval.RxjavaEvaluationStrategy;
import test_data.utilities.Threads;

/**
 * @author Orestis Melkonian
 */
public class Adhoc {

    @Test
    public void mqtt() {

        MqttAsyncClient client = null;
        try {
            client = new MqttAsyncClient("tcp://iot.eclipse.org:1883", "ClientID", new MemoryPersistence());
            client.connect().waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
        }

        Topic<Integer> topic = new Topic<>("OrestisTopic");

        Runnable r1 = new MqttRunnable(
                "node1",
                Stream.range(0,5),
                new SinkOutput<>(topic),
                new RxjavaEvaluationStrategy()
        );
        Runnable r2 = new MqttRunnable(
                "node2",
                Stream.from(topic),
                new ActionOutput<>(System.out::println),
                new RxjavaEvaluationStrategy()
        );
        new Thread(r1).run();
        new Thread(r2).run();

        Threads.sleep();
    }
}
