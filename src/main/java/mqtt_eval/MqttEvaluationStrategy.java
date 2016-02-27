package mqtt_eval;

import mqtt_eval.mqqt_graph.MqttEdge;
import mqtt_eval.mqqt_graph.MqttGraph;
import mqtt_eval.mqqt_graph.MqttNode;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.reactive_ros.Stream;
import org.reactive_ros.evaluation.EvaluationStrategy;
import org.reactive_ros.internal.expressions.MultipleInputExpr;
import org.reactive_ros.internal.expressions.NoInputExpr;
import org.reactive_ros.internal.expressions.SingleInputExpr;
import org.reactive_ros.internal.expressions.Transformer;
import org.reactive_ros.internal.expressions.creation.FromSource;
import org.reactive_ros.internal.graph.FlowGraph;
import org.reactive_ros.internal.output.MultipleOutput;
import org.reactive_ros.internal.output.Output;
import org.reactive_ros.internal.output.SinkOutput;
import org.reactive_ros.util.functions.Func0;
import remote_execution.RemoteExecution;
import remote_execution.StreamTask;

import java.io.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * // TODO add initial task for setting up broker
 * @author Orestis Melkonian
 */
public class MqttEvaluationStrategy implements EvaluationStrategy {

    final RemoteExecution executor = new RemoteExecution();
    Func0<EvaluationStrategy> evaluationStrategy;
    String broker = "tcp://localhost:1884";

    String nodePrefix = "~";
    int topicCounter = 0, nodeCounter = 0;

    /**
     * Constructors
     */
    public MqttEvaluationStrategy(Func0<EvaluationStrategy> evaluationStrategy) {
        this.evaluationStrategy = evaluationStrategy;
    }

    public MqttEvaluationStrategy(Func0<EvaluationStrategy> evaluationStrategy, String broker) {
        this.evaluationStrategy = evaluationStrategy;
        this.broker = broker;
    }

    public MqttEvaluationStrategy(Func0<EvaluationStrategy> evaluationStrategy, String broker, String nodePrefix) {
        this(evaluationStrategy, broker);
        this.nodePrefix = nodePrefix;
    }

    /**
     * Generators
     */
    private String newName() {
        return nodePrefix + "_" + Integer.toString(nodeCounter++);
    }
    public MqttTopic newTopic() {
        return new MqttTopic(nodePrefix + "/" + Integer.toString(topicCounter++));
    }

    /**
     * Evaluation
     */

    private void execute(Queue<StreamTask> tasks) {
        Queue<MqttTask> wrappers = new LinkedList<>(tasks.stream().map(t -> new MqttTask(t, broker, newName())).collect(Collectors.toList()));
        executor.submit(wrappers);
    }

    @Override
    public <T> void evaluate(Stream<T> stream, Output output) {
        FlowGraph flow = stream.getGraph();
        MqttGraph graph = new MqttGraph(flow, this::newTopic);
        Queue<StreamTask> tasks = new LinkedList<>();

        // Run output node first
        MqttTopic result = newTopic();
        tasks.add(new StreamTask(evaluationStrategy, Stream.from(result), output, Collections.singletonList("Mqtt")));

        // Then run each graph vertex as an individual node (reverse BFS)
        Set<MqttNode> checked = new HashSet<>();
        Stack<MqttNode> stack = new Stack<>();
        for (MqttNode root : graph.getRoots())
            new BreadthFirstIterator<>(graph, root).forEachRemaining(stack::push);
        while (!stack.empty()) {
            MqttNode toExecute = stack.pop();
            if (checked.contains(toExecute)) continue;

            Set<MqttEdge> inputs = graph.incomingEdgesOf(toExecute);
            Transformer transformer = toExecute.getTransformer();

            FlowGraph innerGraph = new FlowGraph();
            if (transformer instanceof NoInputExpr) {
                assert inputs.size() == 0;
                // 0 input
                innerGraph.addConnectVertex(transformer);
            } else if (transformer instanceof SingleInputExpr) {
                assert inputs.size() == 1;
                // 1 input
                MqttTopic input = inputs.iterator().next().getTopic();
                Transformer toAdd = new FromSource<>(input.clone());
                innerGraph.addConnectVertex(toAdd);
                innerGraph.attach(transformer);
            } else if (transformer instanceof MultipleInputExpr) {
                assert inputs.size() > 1;
                // N inputs
                innerGraph.setConnectNodes(inputs.stream()
                        .map(edge -> new FromSource(edge.getTopic().clone()))
                        .collect(Collectors.toList()));
                innerGraph.attachMulti(transformer);
            }

            // Set outputs according to graph connections
            Set<MqttEdge> outputs = graph.outgoingEdgesOf(toExecute);
            List<Output> list = new ArrayList<>();
            if (transformer == graph.toConnect)
                list.add(new SinkOutput<>(result.clone()));
            list.addAll(outputs.stream()
                    .map(MqttEdge::getTopic)
                    .map((Function<MqttTopic, SinkOutput<Object>>) (sink) -> {
                        return new SinkOutput(sink);
                    })
                    .collect(Collectors.toList()));
            Output outputToExecute = (list.size() == 1) ? list.get(0) : new MultipleOutput(list);

            // Schedule for execution
            tasks.add(new StreamTask(evaluationStrategy, new Stream(innerGraph), outputToExecute, Collections.singletonList("Mqtt")));

            checked.add(toExecute);
        }

        // Submit the tasks for execution
        execute(tasks);
    }
}
