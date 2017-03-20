package net.coderodde.graph.pathfinding;

import java.util.HashMap;
import java.util.Map;

import net.coderodde.graph.pathfinding.support.Point2DF;

/**
 * This class allows mapping each graph node to its coordinates on a
 * two-dimensional plane.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 6, 2016)
 */
public class DirectedGraphNodeCoordinates {

    /**
     * Maps each node to its coordinates.
     */
    private final Map<Integer, Point2DF> map = new HashMap<>();

    /**
     * Associates the coordinates {@code point} to the node {@code nodeId}.
     *
     * @param nodeId the node to map.
     * @param point  the coordinates to associate to the node.
     */
    public void put(int nodeId, Point2DF point) {
        map.put(nodeId, point);
    }

    /**
     * Return the point of the input node.
     *
     * @param nodeId the node whose coordinates to return.
     * @return the coordinates.
     */
    public Point2DF get(int nodeId) {
        return map.get(nodeId);
    }
}