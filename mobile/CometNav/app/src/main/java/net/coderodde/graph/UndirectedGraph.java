package net.coderodde.graph;

import android.util.Log;

import java.util.Collections;
import java.util.Set;

/**
 * Created by Daniel on 3/23/2017.
 */
/**
 * This class implements an undirected graph data structure via adjacency lists.
 * This implementation represents each graph node as an unique integer. Note that
 * in an undirected graph, the list of children and the list of parents are identical
 * for each node. This is identical to the list of siblings, which is provided as a convenience
 * function.
 *
 * @author Daniel Dehnel
 * @version 1.0 (March 23, 2017)
 */
public class UndirectedGraph extends DirectedGraph{

    /**
     * Creates an undirected arc <tt>(tailNodeId, headNodeId)</tt> if it is not yet
     * present in the graph. In this instance, and undirected graph consists of a directed
     * graph in which every arc which connects two nodes is duplicated going in the opposite
     * direction.
     *
     * @param firstNodeId the first node of the arc.
     * @param secondNodeId the second node of the arc.
     */
    public void addArc(int firstNodeId, int secondNodeId) {
        try
        {
            Set<Integer> set = childMap.get(firstNodeId);
            if (set == null)
                Log.d("Graph", "Attempting to add arc to node " + firstNodeId + ", but node isn't present in graph.");
            else
                set.add(secondNodeId);
            set = childMap.get(secondNodeId);
            if (set == null)
                Log.d("Graph", "Attempting to add arc to node " + secondNodeId + ", but node isn't present in graph.");
            else
                set.add(firstNodeId);
            set = parentMap.get(firstNodeId);
            if (set == null)
                Log.d("Graph", "Attempting to add arc to node " + secondNodeId + ", but node isn't present in graph.");
            else
                set.add(secondNodeId);
            set = parentMap.get(firstNodeId);
            if (set == null)
                Log.d("Graph", "Attempting to add arc to node " + firstNodeId + ", but node isn't present in graph.");
            else
                set.add(secondNodeId);
        }
        catch (NullPointerException e)
        {
            Log.d("Navigation", "startNode " + firstNodeId + " endNode " + secondNodeId + " - " + e.getMessage().toString());
        }
    }

    /**
     * Returns the set of all sibling nodes of the given node {@code nodeId}. Note that
     * in an undirected graph, Siblings are equivalent to both parent and children sets, so just
     * return the parents as it doesn't matter (both are the same)
     *
     * @param nodeId the node whose siblings to return.
     * @return the set of sibling nodes of {@code nodeId}.
     */
    public Set<Integer> getSiblingsOf(int nodeId) {
        return Collections.<Integer>unmodifiableSet(parentMap.get(nodeId));
    }
}
