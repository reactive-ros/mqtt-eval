package mqtt_eval.mqqt_graph;

import mqtt_eval.MqttTopic;
import org.jgrapht.graph.DirectedPseudograph;
import org.reactive_ros.internal.expressions.NoInputExpr;
import org.reactive_ros.internal.expressions.Transformer;
import org.reactive_ros.internal.graph.FlowGraph;
import org.reactive_ros.internal.graph.SimpleEdge;
import org.reactive_ros.util.functions.Func0;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Orestis Melkonian
 */
public class MqttGraph extends DirectedPseudograph<MqttNode, MqttEdge> {
    public Transformer toConnect;

    public MqttGraph(FlowGraph graph, Func0<MqttTopic> topicGenerator) {
        super(MqttEdge.class);
        // Copy FlowGraph
        toConnect = graph.getConnectNode();
        Map<Transformer, MqttNode> mapper = new HashMap<>();
        for (Transformer p : graph.vertexSet()) {
            MqttNode toAdd;
            if (p == graph.getConnectNode()) {
                Transformer connectNode = p.clone();
                toConnect = connectNode;
                toAdd = new MqttNode(connectNode);
            }
            else
                toAdd = new MqttNode(p.clone());
            mapper.put(p, toAdd);
            addVertex(toAdd);
        }
        for (SimpleEdge e : graph.edgeSet()) {
            MqttNode source = mapper.get(e.getSource());
            MqttNode target = mapper.get(e.getTarget());
            addEdge(source, target, new MqttEdge(source, target, topicGenerator.call()));
        }
    }

    public List<MqttNode> predecessors(MqttNode target) {
        return edgeSet().stream().filter(e -> e.getTarget().equals(target)).map(MqttEdge::getSource).collect(Collectors.toList());
    }

    public List<MqttNode> getRoots() {
        return vertexSet().stream().filter(p -> p.getTransformer() instanceof NoInputExpr).collect(Collectors.toList());
    }
}
