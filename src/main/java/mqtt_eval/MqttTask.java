package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.rhea_core.Stream;
import org.rhea_core.evaluation.EvaluationStrategy;
import org.rhea_core.internal.output.Output;
import org.rhea_core.util.functions.Func0;
import remote_execution.StreamTask;

import java.util.List;

/**
 * @author Orestis Melkonian
 */
public class MqttTask extends StreamTask {
    String broker;
    String name;

    public MqttTask(Func0<EvaluationStrategy> strategyGen, Stream stream, Output output, List<String> attr, String broker, String name) {
        super(strategyGen, stream, output, attr);
        this.broker = broker;
        this.name = name;
    }

    public MqttTask(StreamTask task, String broker, String name) {
        this(task.getStrategyGenerator(), task.getStream(), task.getOutput(), task.getRequiredAttributes(), broker, name);
    }

    @Override
    public void run() {
//        System.out.println(this);
        try {
            final MqttAsyncClient client = new MqttAsyncClient(broker, name, new MemoryPersistence());
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            client.connect(options).waitForCompletion();

            for (MqttTopic topic : MqttTopic.extract(stream, output)) {
                client.subscribe(topic.getName(), 2).waitForCompletion();
                topic.setClient(client);
            }
        } catch (MqttException e) {
            stream = Stream.error(e);
        }

        super.run();
    }
}
