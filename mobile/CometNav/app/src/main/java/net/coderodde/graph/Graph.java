package net.coderodde.graph;

/**
 * Created by Daniel on 3/25/2017.
 *
 * Generic Graph interface to be used for both directed and undirected graphs.
 */
public interface Graph {

    public void addArc(int tailNodeId, int headNodeId);

    public void addNode(int nodeId);

}
