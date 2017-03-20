package net.coderodde.graph.pathfinding.support;

/**
 * This class implements an entry for {@link java.util.PriorityQueue}.
 *
 * @author Rodion "rodde" Efremov
 * @version 1.6 (Oct 13, 2016)
 */
final class HeapEntry implements Comparable<HeapEntry> {

    private final int nodeId;
    private final float distance; // The priority key.

    public HeapEntry(int nodeId, float distance) {
        this.nodeId = nodeId;
        this.distance = distance;
    }

    public int getNode() {
        return nodeId;
    }

    public float getDistance() {
        return distance;
    }

    @Override
    public int compareTo(HeapEntry o) {
        return Float.compare(distance, o.distance);
    }
}