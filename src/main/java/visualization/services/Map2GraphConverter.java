package visualization.services;

import common.Line;
import common.Map;
import common.Station;
import org.apache.commons.io.FileUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;

import java.io.File;
import java.io.IOException;

/**
 * Converts a Map used by trace and dissemination engines into a GraphStream Graph that can be visualized.
 */
public class Map2GraphConverter {

    /**
     * Performs the conversion.
     *
     * @param map Map to convert
     * @return A Graph that can be visualized by a VisualizationEngine
     */
    public static Graph convert(Map map) throws IOException {
        Graph graph = getBaseGraphStructure(map);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        String stylesheet = readFile();
        graph.addAttribute("ui.stylesheet", stylesheet);

        return graph;
    }

    private static Graph getBaseGraphStructure(Map map) {
        Graph graph = new MultiGraph("Berlin");

        for (Line line : map.getLines()) {
            for (int i = 0; i < line.getStations().size(); i++) {
                Station currentStation = line.getStations().get(i);
                Station nextStation = null;

                //Check if current station is not the endStation
                if (i != line.getStations().size() - 1) {
                    nextStation = line.getStations().get(i + 1);
                }

                Node currentStationNode = addNodeToGraphIfDoesNotExist(graph, currentStation);

                if (nextStation != null) {
                    Node nextStationNode = addNodeToGraphIfDoesNotExist(graph, nextStation);
                    addEdgeToGraphIfDoesNotExist(graph, currentStation, nextStation, currentStationNode, nextStationNode, line.getName());
                }
            }
        }

        return graph;
    }

    private static void addEdgeToGraphIfDoesNotExist(Graph graph, Station startStation, Station endStation, Node startNode, Node nextStationNode, String lineName) {
        // Connect stations with an edge
        String edgeId = startStation.getId() + "_" + endStation.getId();
        Edge connectingEdge = graph.getEdge(edgeId);
        if (connectingEdge == null) {
            connectingEdge = graph.addEdge(edgeId, startNode, nextStationNode);
            connectingEdge.addAttribute("ui.class", lineName);
        }
    }

    private static Node addNodeToGraphIfDoesNotExist(Graph graph, Station station) {
        // Add the nodes to the graph
        Node stationNode = graph.getNode("" + station.getId());

        if (stationNode == null) {
            stationNode = graph.addNode("" + station.getId());
            stationNode.addAttribute("ui.label", station.getName());

            int[] coords = GeoCoordsUtils.convertToCartesian(station.getLocation());
            stationNode.setAttribute("xyz", coords[0], coords[1], 0);
        }

        return stationNode;
    }

    public static String readFile() throws IOException {
        File file = new File("resources/map-styles.css");

        return FileUtils.readFileToString(file, "utf-8");
    }
}
