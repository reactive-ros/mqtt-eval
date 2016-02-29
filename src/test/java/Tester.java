import mqtt_eval.MqttEvaluationStrategy;
import org.junit.Test;
import org.rhea_core.Stream;
import rx_eval.RxjavaEvaluationStrategy;
import test_data.TestData;
import test_data.TestInfo;
import test_data.utilities.Colors;

/**
 * @author Orestis Melkonian
 */
public class Tester {

    @Test
    public void mqtt_eval() {
        Stream.setEvaluationStrategy(new MqttEvaluationStrategy(RxjavaEvaluationStrategy::new));

        for (TestInfo test : TestData.tests()) {
            System.out.print(test.name + ": ");
            if (test.equality())
                Colors.print(Colors.GREEN, "Passed");
            else {
                Colors.print(Colors.RED, "Failed");
                System.out.println(test.q1 + " != " + test.q2);
            }
        }
    }
}
