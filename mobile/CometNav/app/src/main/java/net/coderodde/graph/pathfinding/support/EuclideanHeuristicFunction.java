package net.coderodde.graph.pathfinding.support;

import java.util.Objects;
import net.coderodde.graph.pathfinding.GraphNodeCoordinates;
import net.coderodde.graph.pathfinding.HeuristicFunction;

/**
 * This class implements a heuristic function that returns the Euclidean
 * distance between two given nodes.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 6, 2016)
 */
public class EuclideanHeuristicFunction implements HeuristicFunction {

    private final GraphNodeCoordinates coordinates;

    public EuclideanHeuristicFunction(GraphNodeCoordinates coordinates) {
        this.coordinates =
                Objects.requireNonNull(coordinates,
                        "The input coordinate map is null.");
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public float estimateDistanceBetween(int nodeId1, int nodeId2) {
        return coordinates.get(nodeId1).distance(coordinates.get(nodeId2));
    }
}