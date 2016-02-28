package mqtt_eval;

import remote_execution.Broker;
import remote_execution.BrokerTask;

/**
 * @author Orestis Melkonian
 */
public class MqttBrokerTask extends BrokerTask {
    public MqttBrokerTask(Broker broker) {
        super(broker);
    }

    @Override
    public void run() {
        try {
            Process p = Runtime.getRuntime().exec("mosquitto -p " + broker.getPort());
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
