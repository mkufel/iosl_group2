package visualization.services;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

import java.util.TimerTask;

public class VisualizationEngine extends TimerTask {

    private int currentTick = 0;

    private Graph graph;

    public VisualizationEngine(Graph graph) {
        this.graph = graph;
    }

    @Override
    public void run() {
        Node n = this.graph.getNode(0);

        if(n.hasAttribute("visited")) {
            n.removeAttribute("visited");
        }
        else {
            n.addAttribute("visited");
        }

        ////////

        for(Node node : this.graph.getEachNode()) {
            if(node.hasAttribute("visited")) {
                node.addAttribute("ui.class", "visited");
            }
            else {
                node.removeAttribute("ui.class");
            }
        }
    }
}
