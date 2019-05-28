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

public class Map2GraphConverter {

    public static Graph convert(Map map) {
        Graph graph = getBaseGraphStructure(map);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

//        Viewer viewer = graph.display();
//        viewer.disableAutoLayout();

        try {
            String stylesheet = readFile();
            graph.addAttribute("ui.stylesheet", stylesheet);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return graph;
    }

    private static Graph getBaseGraphStructure(Map map) {
        Graph graph = new MultiGraph("Berlin");

        for (Line line : map.getLines()) {
            System.out.println("Displaying line: " + line.getName());

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
        String edgeId = startStation.getId() < endStation.getId() ? startStation.getId() + "_" + endStation.getId()
                : endStation.getId() + "_" + startStation.getId();
        Edge connectingEdge = graph.getEdge(edgeId);
        if (connectingEdge == null) {
            connectingEdge = graph.addEdge(edgeId, startNode, nextStationNode);
            connectingEdge.addAttribute("ui.class", lineName);
            System.out.println("Edge created between: " + startStation.getId() + " - " + endStation.getId());
        }
    }

    private static Node addNodeToGraphIfDoesNotExist(Graph graph, Station station) {
        // Add the nodes to the graph
        Node stationNode = graph.getNode("" + station.getId());
        if (stationNode == null) {
            stationNode = graph.addNode("" + station.getId());
            stationNode.addAttribute("ui.label", station.getName());
            int[] coords = GeoCoordsUtils.convertToCartesian(station.getLocation());
            System.out.println("Coordinates Lat/Lon: " + station.getLocation().getLat() + ", " + station.getLocation().getLon());
            System.out.println("Coordinates X/Y: " + coords[0] + ", " + coords[1]);
            stationNode.setAttribute("xyz", coords[0], coords[1], 0);
        }
        return stationNode;
    }


    public static String readFile() throws IOException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        File file = new File("src/main/resources/map-styles.css");

        return FileUtils.readFileToString(file, "utf-8");
    }
}
