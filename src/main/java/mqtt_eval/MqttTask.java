package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.reactive_ros.Stream;
import org.reactive_ros.evaluation.EvaluationStrategy;
import org.reactive_ros.internal.output.Output;
import org.reactive_ros.io.AbstractTopic;
import org.reactive_ros.util.functions.Func0;
import remote_execution.StreamTask;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Orestis Melkonian
 */
public class MqttTask extends StreamTask {
    private String broker;
    private String name;

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

            List<MqttTopic> topics = AbstractTopic.extract(stream, output).stream().map(t -> ((MqttTopic) t)).collect(Collectors.toList());

            for (MqttTopic topic : topics) {
                client.subscribe(topic.getName(), 2).waitForCompletion();
                topic.setClient(client);
            }
        } catch (MqttException e) {
            stream = Stream.error(e);
        }

        super.run();
    }

    @Override
    public String toString() {
        return "\n\n======================== " + info() + " ========================"
                + "\n" + stream.getGraph()
                + "\n\t===>\t" + output + "\n"
                + "\n==================================================\"\n\n";
    }

    private String info() {
        return name + " [" + Thread.currentThread().getId() + "]";
//        return ManagementFactory.getRuntimeMXBean().getName() + "@" + processInfo() + ": ";
    }
}
