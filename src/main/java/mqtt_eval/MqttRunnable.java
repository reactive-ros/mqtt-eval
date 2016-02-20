package mqtt_eval;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.reactive_ros.Stream;
import org.reactive_ros.evaluation.EvaluationStrategy;
import org.reactive_ros.internal.expressions.creation.FromSource;
import org.reactive_ros.internal.output.MultipleOutput;
import org.reactive_ros.internal.output.Output;
import org.reactive_ros.internal.output.SinkOutput;

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


            List<Topic> topics = stream.getGraph()
                    .vertexSet()
                    .stream()
                    .filter(n -> n instanceof FromSource)
                    .map(n -> ((FromSource) n).getSource())
                    .filter(s -> s instanceof Topic)
                    .map(s -> ((Topic) s))
                    .collect(Collectors.toList());

            if (output instanceof MultipleOutput) {
                for (Output out : ((MultipleOutput) output).getOutputs())
                    if (out instanceof SinkOutput && ((SinkOutput) out).getSink() instanceof Topic)
                        topics.add((Topic) ((SinkOutput) out).getSink());
            }
            else if (output instanceof SinkOutput && ((SinkOutput) output).getSink() instanceof Topic)
                topics.add((Topic) ((SinkOutput) output).getSink());

            topics.stream().forEach(n -> {
                try {
                    client.subscribe(n.name, 2).waitForCompletion();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                n.setClient(client);
            });

        } catch (MqttException e) {
            e.printStackTrace();
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
