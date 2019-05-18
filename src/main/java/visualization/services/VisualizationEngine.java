package visualization.services;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.util.TimerTask;

public class VisualizationEngine extends TimerTask {

    private int currentTick = 0;

    private Graph graph;
    private SpriteManager spriteManager;

    public VisualizationEngine(Graph graph) {
        this.graph = graph;
        this.spriteManager = new SpriteManager(graph);

    }


    @Override
    public void run() {
        currentTick += 1;

        drawAndMoveSprites();
        // blinkFirstNode();
    }

    private void drawAndMoveSprites() {

        Sprite sprite = this.spriteManager.getSprite("PERSON1");
        if (sprite == null) {
            sprite = this.spriteManager.addSprite("PERSON1");
            sprite.attachToEdge("1_2");
            sprite.setPosition(0);
        }

        // Move the sprite on every tick
        sprite.setPosition((double) (this.currentTick % 10) / 10);

    }

    private void blinkFirstNode() {
        Node n = this.graph.getNode(0);

        if (n.hasAttribute("visited")) {
            n.removeAttribute("visited");
        } else {
            n.addAttribute("visited");
        }

        ////////

        for (Node node : this.graph.getEachNode()) {
            if (node.hasAttribute("visited")) {
                node.addAttribute("ui.class", "visited");
            } else {
                node.removeAttribute("ui.class");
            }
        }
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;
    }
}
