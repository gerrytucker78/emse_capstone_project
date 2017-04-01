package com.edu.utdallas.argus.cometnav;

import android.util.Log;

import net.coderodde.graph.Demo;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.Graph;
import net.coderodde.graph.UndirectedGraph;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphNodeCoordinates;
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

    public static void testPathfinding()
    {
        UndirectedGraph graph = new UndirectedGraph();
        DirectedGraphNodeCoordinates coordinates = new DirectedGraphNodeCoordinates();

        JSONArray locations, paths;
        try
        {
            locations = new JSONArray
                    (
                            " [\n" +
                                    "\t\t{\"location_id\": 0,\"name\": \"2.HALL.NW.1\",\"type\":\"HALL\",\"pixel_loc_x\":190,\"pixel_loc_y\":105,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 1,\"name\": \"2.HALL.NW.2\",\"type\":\"HALL\",\"pixel_loc_x\":190,\"pixel_loc_y\":180,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 2,\"name\": \"2.201\",\"type\":\"ROOM\",\"pixel_loc_x\":170,\"pixel_loc_y\":190,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 3,\"name\": \"2.202\",\"type\":\"ROOM\",\"pixel_loc_x\":165,\"pixel_loc_y\":178,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 4,\"name\": \"2.203\",\"type\":\"ROOM\",\"pixel_loc_x\":170,\"pixel_loc_y\":170,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 5,\"name\": \"2.204\",\"type\":\"ROOM\",\"pixel_loc_x\":195,\"pixel_loc_y\":100,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 6,\"name\": \"2.2R1\",\"type\":\"ROOM\",\"pixel_loc_x\":198,\"pixel_loc_y\":195,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 7,\"name\": \"2.2R2\",\"type\":\"ROOM\",\"pixel_loc_x\":198,\"pixel_loc_y\":217,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 8,\"name\": \"2.HALL.NW.3\",\"type\":\"HALL\",\"pixel_loc_x\":190,\"pixel_loc_y\":210,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 9,\"name\": \"2.HALL.NW.4\",\"type\":\"HALL\",\"pixel_loc_x\":190,\"pixel_loc_y\":242,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 10,\"name\": \"2.TEMP\",\"type\":\"ROOM\",\"pixel_loc_x\":208,\"pixel_loc_y\":242,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 11,\"name\": \"2.TEMP\",\"type\":\"ROOM\",\"pixel_loc_x\":213,\"pixel_loc_y\":250,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 12,\"name\": \"2.TEMP\",\"type\":\"ROOM\",\"pixel_loc_x\":225,\"pixel_loc_y\":238,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 13,\"name\": \"Blocked 1\",\"type\":\"BLOCKED_AREA\",\"pixel_loc_x\":225,\"pixel_loc_y\":238,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\t\t{\"location_id\": 14,\"name\": \"Blocked 2\",\"type\":\"BLOCKED_AREA\",\"pixel_loc_x\":225,\"pixel_loc_y\":238,\"floor\":2,\"map\":null,\"latlong\":null},\n" +
                                    "\n" +
                                    "\n" +
                                    "\t\t{\"location_id\": 1000,\"name\": \"Floor 2\",\"type\":\"FLOOR\",\"pixel_loc_x\":225,\"pixel_loc_y\":238,\"floor\":2,\"map\":null,\"latlong\":null}\n" +
                                    "\n" +
                                    "]"
                    );

            paths = new JSONArray
                    (
                            "[\n" +
                                    "  {\"start_id\": 0,\"end_id\": 1,\"weight\":null},\n" +
                                    "  {\"start_id\": 1,\"end_id\": 2,\"weight\":null},\n" +
                                    "  {\"start_id\": 1,\"end_id\": 3,\"weight\":null},\n" +
                                    "  {\"start_id\": 1,\"end_id\": 4,\"weight\":null},\n" +
                                    "  {\"start_id\": 1,\"end_id\": 8,\"weight\":null},\n" +
                                    "  {\"start_id\": 8,\"end_id\": 6,\"weight\":null},\n" +
                                    "  {\"start_id\": 8,\"end_id\": 7,\"weight\":null},\n" +
                                    "  {\"start_id\": 8,\"end_id\": 9,\"weight\":null},\n" +
                                    "  {\"start_id\": 9,\"end_id\": 10,\"weight\":null},\n" +
                                    "  {\"start_id\": 10,\"end_id\": 11,\"weight\":null},\n" +
                                    "  {\"start_id\": 11,\"end_id\": 12,\"weight\":null},\n" +
                                    "]"
                    );

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

            for (int i = 0; i < paths.length(); i++)
            {
                JSONObject path = paths.getJSONObject(i);
                graph.addArc(path.getInt("start_id"), path.getInt("end_id"));
            }
        }
        catch (JSONException e)
        {
            Log.d("Navigation", e.getMessage().toString());
        }

        Log.d("Navigation", graph.getNodeSet().toString());

        for (Integer nodeId : graph.getNodeSet())
        {
            //Log.d("Navigation", nodeId + " " + graph.getSiblingsOf(nodeId).toString());
        }

        //This needs to happen after arcs are placed
        DirectedGraphWeightFunction weightFunction =
                Demo.getWeightFunction(graph, coordinates);

        HeuristicFunction hf = new EuclideanHeuristicFunction(coordinates);

        AbstractPathfinder pathfinder = new NBAStarPathfinder(graph,
                weightFunction,
                hf);
        Log.d("Navigation", pathfinder.search(4, 6).toString());
        //Log.d("Navigation", pathfinder.search(5, 1).toString());
    }

    /**
     * Visualize a graph
     * @param graph The graph to visualize
     */
    public static void visualize(Graph graph)
    {

    }
}
