import mqtt_eval.MqttEvaluationStrategy;
import org.junit.Test;
import org.rhea_core.Stream;
import remote_execution.Broker;
import rx_eval.RxjavaEvaluationStrategy;
import test_data.utilities.Threads;

/**
 * @author Orestis Melkonian
 */
public class Adhoc {

    @Test
    public void mqtt() {
        Stream.setEvaluationStrategy(new MqttEvaluationStrategy(RxjavaEvaluationStrategy::new));

        Stream.range(1, 100).subscribe(i -> System.out.println(i));

        Threads.sleep();
    }

}
