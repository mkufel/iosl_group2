package visualization.services;

import common.State;
import common.UserState;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.util.List;
import java.util.TimerTask;

public class VisualizationEngine extends TimerTask {

    private int currentTick = 0;

    private Graph graph;
    private SpriteManager spriteManager;

    private List<State> states;

    public VisualizationEngine(Graph graph, List<State> states) {
        this.graph = graph;
        this.spriteManager = new SpriteManager(graph);
        this.states = states;

    }


    @Override
    public void run() {

        // drawAndMoveSprites();
        // blinkFirstNode();
        drawCurrentState();
        currentTick += 1;

    }

    private void drawCurrentState() {
        // Check if state exists
        if (currentTick > this.states.size() - 1) {
            return;
        }

        System.out.println("Drawing state at tick: " + currentTick);
        State currentState = this.states.get(currentTick);
        for (UserState userState : currentState.getUsers()) {
            this.drawUserState(userState);
        }

    }

    private void drawUserState(UserState userState) {
        Sprite sprite = createSpriteIfDoesNotExist(userState);
        String spriteCurrentEdge = sprite.getAttribute("currentEdge");
        String stateCurrentEdge = userState.getStationStart() + "_" + userState.getStationEnd();

        System.out.println("State edge:" +  stateCurrentEdge);
        if (!spriteCurrentEdge.equals(stateCurrentEdge)) {
            System.out.println("Edges do not match");
            sprite.detach();
            sprite.attachToEdge(userState.getStationStart() + "_" + userState.getStationEnd());
        }

        sprite.setPosition(userState.getProgress());

        if (userState.isData()) {
            sprite.addAttribute("ui.class", "active");
        }
    }

    private Sprite createSpriteIfDoesNotExist(UserState userState) {
        Sprite sprite = this.spriteManager.getSprite("" + userState.getPersonId());
        if (sprite == null) {
            System.out.println("Creating sprite...");
            sprite = this.spriteManager.addSprite("" + userState.getPersonId());
            sprite.attachToEdge(userState.getStationStart() + "_" + userState.getStationEnd());
            sprite.addAttribute("currentEdge", userState.getStationStart() + "_" + userState.getStationEnd());
            sprite.setPosition(0);
        }
        return sprite;
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
