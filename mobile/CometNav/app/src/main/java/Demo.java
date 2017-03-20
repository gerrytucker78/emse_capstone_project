import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.coderodde.graph.DirectedGraph;
import net.coderodde.graph.DirectedGraphWeightFunction;
import net.coderodde.graph.pathfinding.AbstractPathfinder;
import net.coderodde.graph.pathfinding.DirectedGraphNodeCoordinates;
import net.coderodde.graph.pathfinding.DirectedGraphPath;
import net.coderodde.graph.pathfinding.HeuristicFunction;
import net.coderodde.graph.pathfinding.support.AStarPathfinder;
import net.coderodde.graph.pathfinding.support.DijkstraPathfinder;
import net.coderodde.graph.pathfinding.support.EuclideanHeuristicFunction;
import net.coderodde.graph.pathfinding.support.NBAStarPathfinder;
import net.coderodde.graph.pathfinding.support.Point2DF;


/**
 * This class contains a demonstration program comparing performance of three
 * point-to-point shortest path algorithms:
 * <ol>
 *  <li>A*,</li>
 *  <li>Dijkstra's algorithm</li>
 *  <li>NBA*, New Bidirectional A*.</li>
 * </ol>
 *
 * @author Rodion "rodde" Efremov
 * @version 1.61 (Oct 16, 2016)
 */
public class Demo {

    private static final int NODES = 100_000;
    private static final int ARCS =  500_000;
    private static final float PLANE_WIDTH = 1000;
    private static final float PLANE_HEIGHT = 1000;

    public static void main(String[] args) {
        long seed = System.nanoTime();
        Random random = new Random(seed);
        System.out.println("Seed = " + seed);

        long start = System.currentTimeMillis();
        DirectedGraph graph = getRandomGraph(NODES, ARCS, random);
        DirectedGraphNodeCoordinates coordinates = getCoordinates(graph,
                PLANE_WIDTH,
                PLANE_HEIGHT,
                random);
        DirectedGraphWeightFunction weightFunction =
                getWeightFunction(graph, coordinates);

        Integer sourceNodeId = getSource(graph, coordinates);
        Integer targetNodeId = getTarget(graph, coordinates);

        long end = System.currentTimeMillis();

        System.out.println("Created the graph data structures in " +
                (end - start) + " milliseconds.");

        System.out.println("Source: " + sourceNodeId);
        System.out.println("Target: " + targetNodeId);
        System.out.println();

        HeuristicFunction hf = new EuclideanHeuristicFunction(coordinates);

        AbstractPathfinder finder1 = new AStarPathfinder(graph,
                weightFunction,
                hf);

        AbstractPathfinder finder2 = new DijkstraPathfinder(graph,
                weightFunction);

        AbstractPathfinder finder3 = new NBAStarPathfinder(graph,
                weightFunction,
                hf);
        DirectedGraphPath path1 = benchmark(finder1,
                sourceNodeId,
                targetNodeId);

        DirectedGraphPath path2 = benchmark(finder2,
                sourceNodeId,
                targetNodeId);

        DirectedGraphPath path3 = benchmark(finder3,
                sourceNodeId,
                targetNodeId);

        boolean agreed = path1.equals(path2) && path1.equals(path3);

        if (agreed) {
            System.out.println("Algorithms agree: true");
        } else {
            System.out.println("Algorithms DISAGREED!");
            System.out.println("A* path length:       "
                    + path1.getCost(weightFunction));
            System.out.println("Dijkstra path length: "
                    + path2.getCost(weightFunction));
            System.out.println("NBA* path length:     "
                    + path3.getCost(weightFunction));
        }
    }

    private static DirectedGraphPath benchmark(AbstractPathfinder pathfinder,
                                               int sourceNode,
                                               int targetNode) {
        long start = System.currentTimeMillis();
        DirectedGraphPath path = pathfinder.search(sourceNode, targetNode);
        long end = System.currentTimeMillis();

        System.out.println(pathfinder.getClass().getSimpleName() +
                " in " + (end - start) + " milliseconds.");
        System.out.println(path);
        System.out.println();
        return path;
    }

    private static DirectedGraph getRandomGraph(int nodes,
                                                int arcs,
                                                Random random) {
        DirectedGraph graph = new DirectedGraph();

        for (int id = 0; id < nodes; ++id) {
            graph.addNode(id);
        }

        List<Integer> graphNodeList = new ArrayList<>(graph.getNodeSet());

        while (arcs-- > 0) {
            Integer tailNodeId = choose(graphNodeList, random);
            Integer headNodeId = choose(graphNodeList, random);
            graph.addArc(tailNodeId, headNodeId);
        }

        return graph;
    }

    private static DirectedGraphNodeCoordinates
    getCoordinates(DirectedGraph graph,
                   float planeWidth,
                   float planeHeight,
                   Random random) {
        DirectedGraphNodeCoordinates coordinates =
                new DirectedGraphNodeCoordinates();

        for (Integer nodeId : graph.getNodeSet()) {
            coordinates.put(nodeId,
                    randomPoint(planeWidth, planeHeight, random));
        }

        return coordinates;
    }

    private static DirectedGraphWeightFunction
    getWeightFunction(DirectedGraph graph,
                      DirectedGraphNodeCoordinates coordinates) {
        DirectedGraphWeightFunction weightFunction =
                new DirectedGraphWeightFunction();

        for (Integer nodeId : graph.getNodeSet()) {
            Point2DF p1 = coordinates.get(nodeId);

            for (Integer childNodeId : graph.getChildrenOf(nodeId)) {
                Point2DF p2 = coordinates.get(childNodeId);
                float distance = p1.distance(p2);
                weightFunction.put(nodeId, childNodeId, (float)(1.2 * distance));
            }
        }

        return weightFunction;
    }

    private static Point2DF randomPoint(float width,
                                        float height,
                                        Random random) {
        return new Point2DF(width * random.nextFloat(),
                height * random.nextFloat());
    }

    private static <T> T choose(List<T> list, Random random) {
        return list.get(random.nextInt(list.size()));
    }

    private static Integer
    getClosestTo(DirectedGraph graph,
                 DirectedGraphNodeCoordinates coordinates,
                 Point2DF point) {
        double bestDistance = Double.POSITIVE_INFINITY;
        Integer bestNode = null;

        for (Integer node : graph.getNodeSet()) {
            Point2DF nodePoint = coordinates.get(node);

            if (bestDistance > nodePoint.distance(point)) {
                bestDistance = nodePoint.distance(point);
                bestNode = node;
            }
        }

        return bestNode;
    }

    private static Integer getSource(DirectedGraph graph,
                                     DirectedGraphNodeCoordinates coordinates) {
        return getClosestTo(graph, coordinates, new Point2DF());
    }

    private static Integer getTarget(DirectedGraph graph,
                                     DirectedGraphNodeCoordinates coordinates) {
        return getClosestTo(graph,
                coordinates,
                new Point2DF(PLANE_WIDTH, PLANE_HEIGHT));
    }
}