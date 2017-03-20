package net.coderodde.graph.pathfinding.support;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.Set;
import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphPath;
import net.coderodde.graph.pathfinding.HeuristicFunction;
import net.coderodde.graph.pathfinding.TargetUnreachableException;

public final class AStarPathfinder extends AbstractPathfinder {

    private final HeuristicFunction heuristicFunction;
    private final PriorityQueue<HeapEntry> OPEN = new PriorityQueue<>();
    private final Set<Integer> CLOSED = new HashSet<>();
    private final Map<Integer, Float> DISTANCE = new HashMap<>();
    private final Map<Integer, Integer> PARENTS = new HashMap<>();

    public AStarPathfinder(DirectedGraph graph,
                           DirectedGraphWeightFunction weightFunction,
                           HeuristicFunction heuristicFunction) {
        super(graph, weightFunction);
        this.heuristicFunction =
                Objects.requireNonNull(heuristicFunction,
                        "The input heuristic function is null.");
    }

    @Override
    public DirectedGraphPath search(int sourceNodeId, int targetNodeId) {
        init(sourceNodeId);

        while (!OPEN.isEmpty()) {
            Integer currentNodeId = OPEN.remove().getNode();

            if (currentNodeId.equals(targetNodeId)) {
                return tracebackPath(currentNodeId, PARENTS);
            }

            if (CLOSED.contains(currentNodeId)) {
                continue;
            }

            CLOSED.add(currentNodeId);

            for (Integer childNodeId : graph.getChildrenOf(currentNodeId)) {
                if (CLOSED.contains(childNodeId)) {
                    continue;
                }

                float tentativeDistance =
                        DISTANCE.get(currentNodeId) +
                                weightFunction.get(currentNodeId, childNodeId);

                if (!DISTANCE.containsKey(childNodeId)
                        || DISTANCE.get(childNodeId) > tentativeDistance) {
                    DISTANCE.put(childNodeId, tentativeDistance);
                    PARENTS.put(childNodeId, currentNodeId);
                    OPEN.add(
                            new HeapEntry(
                                    childNodeId,
                                    tentativeDistance +
                                            heuristicFunction
                                                    .estimateDistanceBetween(childNodeId,
                                                            targetNodeId)));
                }
            }
        }

        throw new TargetUnreachableException(graph, sourceNodeId, targetNodeId);
    }

    private void init(int sourceNodeId) {
        OPEN.clear();
        CLOSED.clear();
        PARENTS.clear();
        DISTANCE.clear();

        OPEN.add(new HeapEntry(sourceNodeId, (float) 0));
        PARENTS.put(sourceNodeId, null);
        DISTANCE.put(sourceNodeId, (float) 0);
    }
}