import mqtt_eval.MqttEvaluationStrategy;
import org.junit.Test;
import org.reactive_ros.Stream;
import rx_eval.RxjavaEvaluationStrategy;
import test_data.utilities.Threads;

/**
 * @author Orestis Melkonian
 */
public class Tester {

    @Test
    public void mqtt_eval() {
        Stream.setEvaluationStrategy(new MqttEvaluationStrategy(() -> new RxjavaEvaluationStrategy()));

        Stream.range(1, 100).subscribe(i -> System.out.println(i));

        Threads.sleep();
    }
}