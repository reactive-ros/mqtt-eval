package mqtt_eval;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.rhea_core.Stream;
import org.rhea_core.evaluation.EvaluationStrategy;
import org.rhea_core.internal.output.Output;

/**
 * @author Orestis Melkonian
 */
public class MqttEvaluationStrategy implements EvaluationStrategy {

    EvaluationStrategy innerStrategy;
    String broker;
    String clientName;

    public MqttEvaluationStrategy(EvaluationStrategy innerStrategy, String clientName) {
        this.innerStrategy = innerStrategy;
        this.broker = "tcp://m2m.eclipse.org:1883"; // default broker
        this.clientName = clientName;
    }

    public MqttEvaluationStrategy(EvaluationStrategy innerStrategy, String broker, String clientName) {
        this.innerStrategy = innerStrategy;
        this.broker = broker;
        this.clientName = clientName;
    }

    @Override
    public <T> void evaluate(Stream<T> stream, Output output) {
        // Set client
        try {
            final MqttAsyncClient client = new MqttAsyncClient(broker, clientName, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options).waitForCompletion();

            for (MqttTopic topic : MqttTopic.extract(stream, output)) {
                client.subscribe(topic.getName(), 2).waitForCompletion();
                topic.setClient(client);
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }

        // Propagate evaluation to first-order strategy
        innerStrategy.evaluate(stream, output);
    }
}
