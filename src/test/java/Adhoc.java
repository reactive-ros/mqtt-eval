import mqtt_eval.MqttEvaluationStrategy;
import org.junit.Test;
import org.rhea_core.Stream;
import remote_execution.Broker;
import rx_eval.RxjavaEvaluationStrategy;
import test_data.utilities.Threads;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Orestis Melkonian
 */
public class Adhoc {

    @Test
    public void mqtt() {
        Stream.setEvaluationStrategy(new MqttEvaluationStrategy(RxjavaEvaluationStrategy::new));

        /*Queue<Integer> q = new ConcurrentLinkedDeque<>();
        Stream.range(1, 100).subscribe(i -> {
            System.out.println(q);
            q.add(i);
            System.out.println(q);
        });*/
//        Stream.nat().subscribe(i -> System.out.println(i));

        /*Stream<Integer> s1 = Stream.just(0, 1, 2);
        Stream<Integer> s2 = Stream.just(3, 4, 5);
        Stream.concat(s1, s2).subscribe(i -> System.out.println(i));*/

        Stream.range(0, 10).id().id().id().id().id().id().id().id().id().id().id().id().id().id().subscribe(i -> System.out.println(i));

        Threads.sleep();
    }

}
