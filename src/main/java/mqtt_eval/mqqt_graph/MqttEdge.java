package mqtt_eval.mqqt_graph;

import mqtt_eval.MqttTopic;
import org.jgrapht.graph.DefaultEdge;

/**
 * @author Orestis Melkonian
 */
public class MqttEdge extends DefaultEdge {
    private MqttNode source;
    private MqttNode target;
    private MqttTopic topic;

    public MqttEdge(MqttNode v1, MqttNode v2, MqttTopic topic) {
        this.source = v1;
        this.target = v2;
        this.topic = topic;
    }

    public MqttTopic getTopic() {
        return topic;
    }

    @Override
    public MqttNode getSource() {
        return source;
    }

    @Override
    public MqttNode getTarget() {
        return target;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj != null) && (obj instanceof MqttEdge)
                && ((MqttEdge) obj).getSource().equals(source)
                && ((MqttEdge) obj).getTarget().equals(target);
    }

    @Override
    public String toString() {
        return topic.getName();
    }
}
