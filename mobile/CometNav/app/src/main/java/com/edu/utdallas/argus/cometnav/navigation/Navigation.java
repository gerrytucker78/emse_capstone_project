package com.edu.utdallas.argus.cometnav.navigation;

import android.util.Log;

import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.Graph;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphPath;
import net.coderodde.graph.pathfinding.GraphNodeCoordinates;
import net.coderodde.graph.pathfinding.HeuristicFunction;
import net.coderodde.graph.pathfinding.support.EuclideanHeuristicFunction;
import net.coderodde.graph.pathfinding.support.NBAStarPathfinder;
import net.coderodde.graph.pathfinding.support.Point2DF;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Handler;

import com.edu.utdallas.argus.cometnav.dataservices.beacons.CometNavBeacon;
import com.edu.utdallas.argus.cometnav.dataservices.DataServices;
import com.edu.utdallas.argus.cometnav.dataservices.locations.ILocationClient;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Location;
import com.edu.utdallas.argus.cometnav.dataservices.locations.Path;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.coderodde.graph.UndirectedGraph;

import static java.lang.Math.sqrt;

/**
 * Created by Daniel on 3/25/2017.
 */

public class Navigation implements ILocationClient {
    private static final String TAG = Navigation.class.toString();
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

    private Timer navTimer;

    private TimerTask timerTask;

    private final Handler handler = new Handler();

    /**
     * The current node id.
     */
    private int currentNode;

    /**
     * The start node ID
     */
    private int startNode;

    /**
     * The target node id, if any
     */
    private int endNode;

    private HashMap<Integer, CometNavBeacon> beaconMap;

    private List<Location> navigableLocations;

    private CurrentLocation currentLocation;

    /**
     * Hashmap of beacon names (an int) to a CometNavBeacon object
     */
    public HashMap<Integer, CometNavBeacon> getBeaconMap() {
        return beaconMap;
    }

    public void setBeaconMap(HashMap<Integer, CometNavBeacon> beaconMap) {
        this.beaconMap = beaconMap;
    }


    private static final Navigation navigation = new Navigation();

    public static Navigation getInstance() {
        return navigation;
    }

    private Navigation() {
        graph = new UndirectedGraph();
        coordinates = new GraphNodeCoordinates();
        setBeaconMap(new HashMap<>());
        mRouteChangedListeners = new ArrayList<OnRouteChangedListener>();

        DataServices.getNavigableLocations(this);
        DataServices.getBeacons(this);

    }


    public void updateBeacons(JSONArray beacons) {
        try {
            for (int i = 0; i < beacons.length(); i++) {
                JSONObject beacon = beacons.getJSONObject(i);
                CometNavBeacon cnBeacon = new CometNavBeacon(beacon);
                getBeaconMap().put(cnBeacon.getName(), cnBeacon);
            }
        } catch (JSONException | NullPointerException e) {
            Log.d("Navigation", e.toString());
        }
        Log.d("Navigation", "Beacons updated: " + getBeaconMap().toString());
    }

    /**
     * Begins navigation from the current node to the target node
     *
     * @param targetNodeId the target node to navigate to
     */
    public void beginNavigation(int targetNodeId) {
        //translate our current location into a node ID
        beginNavigation(findNearestNode(), targetNodeId);
    }

    /**
     * Finds the nearest exit and navigates from our current location
     */
    public void beginEmergencyNavigation()
    {
        int currentNode = findNearestNode();
        if (currentNode == 0)
        {
            Log.d("Navigation", "Warning! Can't find current node, so can't start emergency nav!");
            return;
        }
        int exitNode = 0;
        int[] exitRoute;
        float minWeight = Float.MAX_VALUE;
        //First find the nearest exit
        for (Location loc : navigableLocations)
        {
            if (loc.getType() == Location.Type.EXIT)
            {
                exitRoute = pathfinder.search(currentNode, loc.getLocationId()).toArray();
                float weight = getWeightOfPath(exitRoute);
                Log.d("Test", Arrays.toString(exitRoute) + " weight is " + weight);
                if (weight < minWeight) {
                    minWeight = weight;
                    exitNode = loc.getLocationId();
                }
            }
        }
        if (exitNode != 0)
            beginNavigation(exitNode);
        else
            Log.d("Navigation", "Warning! Cannot find exit!");
    }

    private float getWeightOfPath(int[] path)
    {
        float sum = 0;
        for (int i=0; i < path.length; i++)
        {
            sum += weightFunction.get(i, i+1);
        }
        return sum;
    }

    /**
     * Begins navigation from the start node to the end node
     *
     * @param startNodeId the start node to begin navigating from
     * @param endNodeId   the target node to navigate to
     */
    public void beginNavigation(int startNodeId, int endNodeId) {
        startNode = startNodeId;
        endNode = endNodeId;
        Log.d("Navigation", "Beginning navigation from " + startNodeId + " to " + endNodeId);
        if (pathfinder != null) {
            DataServices.getBlockedAreas(Navigation.this);
            updateCurrentRoute();
        }
        startNavTimer();
    }

    /**
     *
     */
    public void stopNavigation() {
        navTimer.cancel();
    }

    /**
     * Given a node ID, return the coords for that node
     * @param nodeId
     * @return
     */
    public int[] getNodePos(int nodeId) {
        int[] retArray = new int[2];
        Point2DF point = coordinates.get(nodeId);
        if (point != null) {
            retArray[0] = Math.round(point.getX());
            retArray[1] = Math.round(point.getY());
        }
        return retArray;
    }

    /**
     * Returns the location object for the given node ID
     * @param nodeId
     * @return
     */
    public Location getLocation (int nodeId)
    {
        for (Location loc : navigableLocations)
        {
            if (loc.getLocationId() == nodeId)
                return loc;
        }
        return null;
    }

    public CurrentLocation calculateCurrentPos(List<CometNavBeacon> beaconList) {
        int iterator = 0;
        //We need at least 3 beacons to preform trilateration.
        int count = 0;
        ArrayList<ArrayList<Double>> posList = new ArrayList<>();
        ArrayList<Double> distanceList = new ArrayList<>();
        ArrayList<Double> floorList = new ArrayList<>();
        CurrentLocation loc = new CurrentLocation();
        boolean locationFound = false;

        CometNavBeacon closestBeacon = null;
        double minDistance = Double.MAX_VALUE;

        for (CometNavBeacon beacon : beaconList) {
            //foundBeacon is a known beacon we have stored on the web server. It contains the
            //beacon coordinates.
            //beacon is one of the beacons we are currently detecting. It contains the beacon's
            //distance.
            Log.d("Navigation", "Beacon in beaconList: " + beacon.toString());

            CometNavBeacon foundBeacon = getBeaconMap().get(beacon.getName());
            if (foundBeacon != null) {
                count++;
                ArrayList<Double> list = new ArrayList<Double>();
                list.add((double) foundBeacon.getxLoc());
                list.add((double) foundBeacon.getyLoc());
                //Iffy on this one
                //list.add((double) foundBeacon.getFloor());
                posList.add(list);
                distanceList.add(beacon.getDistance());
                floorList.add((double) foundBeacon.getFloor());
                if (beacon.getDistance() < minDistance)
                {
                    minDistance = beacon.getDistance();
                    closestBeacon = beacon;
                }
            }
        }
        if (count == 0) {
            Log.d("Navigation", "Warning! no beacons in list, cannot determine position");
        } else if (count == 1) {
            //If we see only 1 beacon, we are effectively a circle of radius distance around
            //said beacon on its floor. We can't know anything else.
            //return point and radius
            loc.setxLoc((int) Math.round(posList.get(0).get(0)));
            loc.setyLoc((int) Math.round(posList.get(0).get(1)));
            //loc.setFloor((int) Math.round(posList.get(0).get(2)));
            loc.setRadius((int) Math.round(distanceList.get(0)));
            locationFound = true;
        } else {
            //With 2 points we can do a 1d trilateration which returns an estimated point that is
            //fairly innacurate. 3 points is where we really can trilaterate our position.
            //First determine if the beacons are on the same floor
            double[][] positions = new double[posList.size()][];
            for (int i = 0; i < posList.size(); i++) {
                ArrayList<Double> row = posList.get(i);
                positions[i] = new double[row.size()];
                for (int j = 0; j < row.size(); j++) {
                    positions[i][j] = row.get(j).doubleValue();
                }
            }
            double[] distances = new double[distanceList.size()];
            for (int i = 0; i < distanceList.size(); i++) {
                distances[i] = distanceList.get(i).doubleValue();
            }
            double[] resultPoint = LocationFinder.getLocationPoint(positions, distances);
            loc.setxLoc((int) Math.round(resultPoint[0]));
            loc.setyLoc((int) Math.round(resultPoint[1]));
            //loc.setFloor((int) Math.round(resultPoint[2]));
            locationFound = true;
        }

        if (closestBeacon != null) {
            //snap to radius around closest beacon
            Point2DF pointClosestToBeacon =
                    closestPointToCircle(loc.getxLoc(), loc.getyLoc(),
                            closestBeacon.getxLoc(), closestBeacon.getyLoc(), closestBeacon.getDistance());

            loc.setxLoc(Math.round(pointClosestToBeacon.getX()));
            loc.setyLoc(Math.round(pointClosestToBeacon.getY()));
        }
        currentLocation = loc;
        return loc;
    }

    private int findNearestNode()
    {
        if (currentLocation == null)
            return 0;
        // Look for nearby navigable location to projected location and set to the nearest
        double smallestZ = 0;
        Location closestNavLoc = null;
        for (Location navLoc : this.navigableLocations) {
            if (navLoc.getFloor() == currentLocation.getFloor()) {
                double x = currentLocation.getxLoc() - navLoc.getPixelLocX();
                double y = currentLocation.getyLoc() - navLoc.getPixelLocY();
                double z = 0;
                z = (y * y) + (x * x);
                if (z > 0 && (smallestZ == 0 || (z < smallestZ))) {
                    smallestZ = z;
                    closestNavLoc = navLoc;
                }
            }
        }
        //The closest node is contained in Location.
        return closestNavLoc.getLocationId();
    }

    private Point2DF closestPointToCircle(double pX, double pY, double cX, double cY, double radius)
    {
        double vX = pX - cX;
        double vY = pY - cY;
        double magV = sqrt(vX*vX + vY*vY);
        double aX = cX + vX / magV * radius;
        double aY = cY + vY / magV * radius;
        return new Point2DF((float)aX, (float)aY);
    }

    private List<OnRouteChangedListener> mRouteChangedListeners;

    public void setOnRouteChangedListener(OnRouteChangedListener listener) {
        mRouteChangedListeners.add(listener);
    }

    /**
     * Updates the current route and notifies listeners
     */
    private void updateCurrentRoute() {
        if (pathfinder != null) {
            currentRoute = pathfinder.search(startNode, endNode);
            Log.d("Navigation", currentRoute.toString());
            int[] notifyArray = currentRoute.toArray();
            //Log.d("Navigation", notifyArray.toString());
            //notify listeners
            for (OnRouteChangedListener listener : mRouteChangedListeners) {
                listener.onRouteChange(notifyArray);
            }
        }
    }

    /**
     * Populates the weight function. Should be run after updating the graph and coordinates.
     */
    private void populateWeightFunction() {
        weightFunction = new DirectedGraphWeightFunction();

        for (Integer nodeId : graph.getNodeSet()) {
            Point2DF p1 = coordinates.get(nodeId);
            for (Integer childNodeId : graph.getChildrenOf(nodeId)) {
                Point2DF p2 = coordinates.get(childNodeId);

                float distance = p1.distance(p2);
                weightFunction.put(nodeId, childNodeId, (float) (1.2 * distance));
            }
        }
    }

    private void startNavTimer() {
        //set a new Timer
        navTimer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, after the first 2 seconds the TimerTask will run every 2 seconds
        navTimer.schedule(timerTask, 2000, 2000); //
    }

    private void stopNavTimer() {
        //stop the timer, if it's not already null
        if (navTimer != null) {
            navTimer.cancel();
            navTimer = null;
        }
    }

    private void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        DataServices.getBlockedAreas(Navigation.this);
                        updateCurrentRoute();
                    }
                });
            }
        };
    }

    /**
     * Updates the internal graph model with the new locations. Expected to run before updatePaths.
     *
     * @param locations
     */
    @Override
    public void receiveNavigableLocations(List<Location> locations) {
        this.navigableLocations = locations;

        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);

            if (location.getType() == Location.Type.HALL || location.getType() == Location.Type.ROOM || location.getType() == Location.Type.EXIT
                    || location.getType() == Location.Type.STAIRS) {
                graph.addNode(location.getLocationId());
                coordinates.put(location.getLocationId(), new Point2DF(location.getPixelLocX(), location.getPixelLocY()));
            }
        }
        Log.d("Navigation", "Nodes updated: " + graph.getNodeSet().toString());
        //Paths has to happen after locations is done.
        DataServices.getPaths(this);
    }

    /**
     * Updates the internal graph model with the new blocked areas
     *
     * @param locations
     */
    @Override
    public void receiveBlockedAreas(List<Location> locations) {
        for (int i = 0; i < locations.size(); i++) {
            Location location = locations.get(i);
            graph.detach(location.getLocationId());
        }
    }

    /**
     * Updates the internal graph model with the new arcs, then finishes updating the graph by
     * creating the weight function and the heuristics and pathfinder. Expected to run after
     * updateNodes
     *
     * @param paths
     */
    @Override
    public void receivePaths(List<Path> paths) {
        String logStr = new String();
        for (int i = 0; i < paths.size(); i++) {
            Path path = paths.get(i);
            graph.addArc(path.getStartId(), path.getEndId());
            logStr += path.getStartId() + "-" + path.getEndId() + ", ";
        }

        Log.d("Navigation", "Arcs updated: " + logStr);
        //This needs to happen after arcs are placed
        populateWeightFunction();

        heuristics = new EuclideanHeuristicFunction(coordinates);
        pathfinder = new NBAStarPathfinder(graph, weightFunction, heuristics);
    }

}

