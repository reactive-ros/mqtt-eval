package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.reactive_ros.Stream;
import org.reactive_ros.evaluation.EvaluationStrategy;
import org.reactive_ros.internal.expressions.creation.FromSource;
import org.reactive_ros.internal.output.MultipleOutput;
import org.reactive_ros.internal.output.Output;
import org.reactive_ros.internal.output.SinkOutput;
import org.reactive_ros.io.AbstractTopic;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Orestis Melkonian
 */
public class MqttRunnable implements Runnable {
    private static final String BROKER = "tcp://localhost:1884";
    public String name;
    public Stream stream;
    public Output output;
    public EvaluationStrategy evaluationStrategy;

    public MqttRunnable(String name, Stream stream, Output output, EvaluationStrategy evaluationStrategy) {
        this.name = name;
        this.stream = stream;
        this.output = output;
        this.evaluationStrategy = evaluationStrategy;
    }

    @Override
    public void run() {
//        display();
        try {
            final MqttAsyncClient client = new MqttAsyncClient(BROKER, name, new MemoryPersistence());
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

        evaluationStrategy.evaluate(stream, output);
    }

    private void display() {
        System.out.println(
                "\n\n======================== " + info() + " ========================"
                        + "\n" + stream.getGraph()
                        + "\n\t===>\t" + output + "\n"
                        + "\n==================================================\"\n\n");
    }

    private String info() {
        return name + " [" + Thread.currentThread().getId() + "]";
//        return ManagementFactory.getRuntimeMXBean().getName() + "@" + processInfo() + ": ";
    }
}
