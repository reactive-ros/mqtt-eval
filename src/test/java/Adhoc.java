import org.junit.Test;
import org.rhea_core.Stream;
import org.rhea_core.internal.Notification;

import mqtt_eval.MqttEvaluationStrategy;
import mqtt_eval.MqttInternalTopic;
import mqtt_eval.MqttTopic;
import rx_eval.RxjavaEvaluationStrategy;
import test_data.utilities.Threads;

/**
 * @author Orestis Melkonian
 */
public class Adhoc {

//    @Test
    public void mqtt() {
        Stream.evaluationStrategy =
                new MqttEvaluationStrategy(new RxjavaEvaluationStrategy(), "myclientname2");

        MqttTopic testTopic = new MqttTopic("test_topic2");

        Stream.from(testTopic)
                .map(String::new)
                .print();

        Stream.just("TestString1", "TestString2", "TestString3", "TestString4", "TestString5")
                .map(String::getBytes)
                .subscribe(testTopic);

        Threads.sleep();
    }

    @Test
    public void mqtt_internal() {
        Stream.evaluationStrategy =
                new MqttEvaluationStrategy(new RxjavaEvaluationStrategy(), "myclientname10");

        MqttInternalTopic<String> testTopic = new MqttInternalTopic<>("test_topic10");

        Stream.from(testTopic)
                .print();

        Stream.just("TestString1", "TestString2", "TestString3", "TestString4", "TestString5")
                .subscribe(testTopic);

        Threads.sleep();
    }

}
