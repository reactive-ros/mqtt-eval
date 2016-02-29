package mqtt_eval.mqqt_graph;

import org.rhea_core.internal.expressions.Transformer;

/**
 * @author Orestis Melkonian
 */
public class MqttNode {
    private Transformer transformer;

    public MqttNode(Transformer transformer) {
        this.transformer = transformer;
    }

    public Transformer getTransformer() {
        return transformer;
    }

    @Override
    public int hashCode() {
        return transformer.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return !(obj == null || !(obj instanceof MqttNode)) && transformer.equals(((MqttNode) obj).transformer);
    }

    @Override
    public String toString() {
        return transformer.toString();
    }
}
