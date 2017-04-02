package com.edu.utdallas.argus.cometnav;

import android.util.Log;

import net.coderodde.graph.Demo;
import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.Graph;
import net.coderodde.graph.UndirectedGraph;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphPath;
import net.coderodde.graph.pathfinding.GraphNodeCoordinates;
import net.coderodde.graph.pathfinding.HeuristicFunction;
import net.coderodde.graph.pathfinding.support.EuclideanHeuristicFunction;
import net.coderodde.graph.pathfinding.support.NBAStarPathfinder;
import net.coderodde.graph.pathfinding.support.Point2DF;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Daniel on 3/25/2017.
 */

public class Navigation {

    /**
     * The node IDs mapped to coordinates
     */
    private GraphNodeCoordinates coordinates;

    /**
     * The latest graph used for navigation
     */
    private UndirectedGraph graph;

    /**
     * The current route, if we're currently navigating
     */
    private DirectedGraphPath currentRoute;

    /**
     * The weight function. Updated after the graph is updated.
     */
    private DirectedGraphWeightFunction weightFunction;

    /**
     * The Heuristic function is used to facilitate rapid speed up of the pathfinding algorithm.
     */
    private HeuristicFunction heuristics;

    /**
     * The pathfinder we use to navigate.
     */
    private AbstractPathfinder pathfinder;

    /**
     * The current node id.
     */
    private int currentNode;

    /**
     * The target node id, if any
     */
    private int endNode;

    public enum locTypes
    {
        BLOCKED_AREA,
        ROOM,
        HALL,
        UNKNOWN
    };

    static locTypes toLocEnum(String str)
    {
        if (str.equals("BLOCKED_AREA"))
            return locTypes.BLOCKED_AREA;
        else if (str.equals("ROOM"))
            return locTypes.ROOM;
        else if (str.equals("HALL"))
            return locTypes.HALL;
        return locTypes.UNKNOWN;
    }

    public Navigation()
    {
        graph = new UndirectedGraph();
        coordinates = new GraphNodeCoordinates();
    }

    /**
     * Updates the internal graph model with the new locations. Expected to run before updatePaths.
     * @param locations Expected to be a JSON array populated with the list of nodes.
     */
    public void updateNodes(JSONArray locations)
    {
        try
        {
            for(int i = 0; i < locations.length(); i++)
            {
                JSONObject location = locations.getJSONObject(i);
                locTypes type = toLocEnum(location.getString("type"));
                //Log.d("Navigation", "Found location " + location.getInt("location_id") + " " + location.getString("type"));
                if (type == locTypes.HALL || type == locTypes.ROOM)
                {
                    int nodeId = location.getInt("location_id");
                    graph.addNode(nodeId);
                    coordinates.put(nodeId, new Point2DF(location.getInt("pixel_loc_x"), location.getInt("pixel_loc_y")));
                }
            }
        }
        catch (JSONException e)
        {
            Log.d("Navigation", e.getMessage().toString());
        }
        Log.d("Navigation", "Nodes updated: " + graph.getNodeSet().toString());
    }

    /**
     * Updates the internal graph model with the new arcs, then finishes updating the graph by
     * creating the weight function and the heuristics and pathfinder. Expected to run after
     * updateNodes
     * @param paths Expected to be a JSON array populated with a list of the arcs (paths) between
     *              nodes.
     */
    public void updatePaths(JSONArray paths)
    {
        String logStr = new String();
        try
        {
            for (int i = 0; i < paths.length(); i++)
            {
                JSONObject path = paths.getJSONObject(i);
                int startNode = path.getInt("start_id");
                int endNode = path.getInt("end_id");
                graph.addArc(startNode, endNode);
                logStr += startNode + "-" + endNode + ", ";
            }
        }
        catch (JSONException e)
        {
            Log.d("Navigation", e.getMessage().toString());
        }


        Log.d("Navigation", "Arcs updated: " + logStr);
        //This needs to happen after arcs are placed
        populateWeightFunction();

        heuristics = new EuclideanHeuristicFunction(coordinates);
        pathfinder = new NBAStarPathfinder(graph, weightFunction, heuristics);
    }

    /**
     * Begins navigation from the current node to the target node
     * @param targetNodeId the target node to navigate to
     */
    public void beginNavigation(int targetNodeId)
    {

    }

    /**
     * Begins navigation from the start node to the end node
     * @param startNodeId the start node to begin navigating from
     * @param endNodeId the target node to navigate to
     */
    public void beginNavigation(int startNodeId, int endNodeId)
    {

    }

    /**
     *
     */
    public void updateCurrentRoute()
    {
        currentRoute = pathfinder.search(currentNode, endNode);
        Log.d("Navigation", currentRoute.toString());
    }

    /**
     * Populates the weight function. Should be run after updating the graph and coordinates.
     */
    private void populateWeightFunction()
    {
        weightFunction = new DirectedGraphWeightFunction();

        for (Integer nodeId : graph.getNodeSet())
        {
            Point2DF p1 = coordinates.get(nodeId);
            for (Integer childNodeId : graph.getChildrenOf(nodeId))
            {
                Point2DF p2 = coordinates.get(childNodeId);
                float distance = p1.distance(p2);
                weightFunction.put(nodeId, childNodeId, (float)(1.2 * distance));
            }
        }
    }

    /**
     * Visualize a graph
     * @param graph The graph to visualize
     */
    public static void visualize(Graph graph)
    {

    }
}
