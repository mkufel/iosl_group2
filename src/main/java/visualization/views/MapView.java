package visualization.views;

import common.Line;
import common.Map;
import common.Station;
import org.apache.commons.io.FileUtils;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.Viewer;
import visualization.services.GeoCoordsUtils;
import visualization.services.MapFactory;

import java.io.File;
import java.io.IOException;

public class MapView {

    private MapFactory mapFactory = new MapFactory();

    private Graph getBaseGraphStructure(Map map) {
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
                    addEdgeToGraphIfDoesNotExist(graph, currentStation, nextStation, currentStationNode, nextStationNode);
                }

            }

        }

        return graph;

    }

    private void addEdgeToGraphIfDoesNotExist(Graph graph, Station startStation, Station endStation, Node startNode, Node nextStationNode) {
        // Connect stations with an edge
        String edgeId = startStation.getId() + "_" + endStation.getId();
        Edge connectingEdge = graph.getEdge(edgeId);
        if (connectingEdge == null) {
            connectingEdge = graph.addEdge(edgeId, startNode, nextStationNode);
            System.out.println("Edge created between: " + startStation.getId() + " - " + endStation.getId());
        }
    }

    private Node addNodeToGraphIfDoesNotExist(Graph graph, Station station) {
        // Add the nodes to the graph
        Node stationNode = graph.getNode(station.getId());
        if (stationNode == null) {
            stationNode = graph.addNode(station.getId());
            int[] coords = GeoCoordsUtils.convertToCartesian(station.getLocation());
            System.out.println("Coordinates Lat/Lon: " + station.getLocation().getLat() + ", " + station.getLocation().getLon());
            System.out.println("Coordinates X/Y: " + coords[0] + ", " + coords[1]);
            stationNode.setAttribute("xyz", coords[0], coords[1], 0);
        }
        return stationNode;
    }

    public void showGraph() {
        Graph graph = this.getBaseGraphStructure(mapFactory.createMap());
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        Viewer viewer = graph.display();
        viewer.disableAutoLayout();
        try {
            String stylesheet = readFile();
            graph.addAttribute("ui.stylesheet", stylesheet);
        } catch (IOException e) {
            e.printStackTrace();
        }



    }

    public String readFile() throws IOException {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));
        File file = new File("src/main/resources/map-styles.css");
        return FileUtils.readFileToString(file, "utf-8");
    }
}
