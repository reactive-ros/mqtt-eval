package mqtt_eval;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.rhea_core.Stream;
import org.rhea_core.annotations.StrategyInfo;
import org.rhea_core.evaluation.EvaluationStrategy;
import org.rhea_core.internal.output.Output;

/**
 * @author Orestis Melkonian
 */
@StrategyInfo(name = "mqtt", requiredSkills = {"Mqtt"}, priority = 1)
public class MqttEvaluationStrategy implements EvaluationStrategy {

    EvaluationStrategy innerStrategy;
    MqttAsyncClient client;


    public MqttEvaluationStrategy(EvaluationStrategy innerStrategy, String clientName) {
        this(innerStrategy, "tcp://m2m.eclipse.org:1883", clientName); // default broker
    }

    public MqttEvaluationStrategy(EvaluationStrategy innerStrategy, String broker, String clientName) {
        this.innerStrategy = innerStrategy;
        try {
            client = new MqttAsyncClient(broker, clientName, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options).waitForCompletion();
        } catch (MqttException e) {
            e.printStackTrace();
            client = null;
        }
    }

    @Override
    public <T> void evaluate(Stream<T> stream, Output output) {
        // Set client
        for (MqttTopic topic : MqttTopic.extract(stream, output)) {
            try {
                client.subscribe(topic.getName(), 2).waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            topic.setClient(client);
        }
        for (MqttInternalTopic topic : MqttInternalTopic.extract(stream, output)) {
            try {
                client.subscribe(topic.getName(), 2).waitForCompletion();
            } catch (MqttException e) {
                e.printStackTrace();
            }
            topic.setClient(client);
        }

        // Propagate evaluation to first-order strategy
        innerStrategy.evaluate(stream, output);
    }
}
