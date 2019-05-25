package visualization;

import common.State;
import common.UserState;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

public class VisualizationEngine extends TimerTask {

    private int currentTick = 0;

    private boolean isRunning = false;
    private Graph graph;
    private Viewer viewer;
    private ViewPanel viewPanel;
    private SpriteManager spriteManager;

    private List<State> states;

    private OnVisualizationStateChangedListener onVisualizationStateChangedListener;

    public VisualizationEngine(Graph graph, List<State> states) {
        this.graph = graph;
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        this.viewer = new Viewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        this.viewer.disableAutoLayout();
        this.viewer.enableXYZfeedback(true);
        this.viewPanel = this.viewer.addDefaultView(false);
        this.spriteManager = new SpriteManager(graph);
        this.states = states;
    }

    public int getCurrentTick() {
        return currentTick;
    }

    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;

        // If the simulation is not running we should visualize manual movement of states/ticks
        if (!isRunning) {
            this.drawCurrentState();
        }
    }

    public ViewPanel getViewPanel() {
        return this.viewPanel;
    }

    @Override
    public void run() {
        if (this.isRunning) {
            drawCurrentState();
            currentTick += 1;
        }

    }

    private void drawCurrentState() {
        // Check if state exists
        if (currentTick > this.states.size() - 1 || currentTick < 0) {
            this.isRunning = false;
            return;
        }

        List<Sprite> toRemoveSprites = new ArrayList<>();
        this.spriteManager.iterator().forEachRemaining(toRemoveSprites::add);

        for (Sprite sprite : toRemoveSprites) {
            this.spriteManager.removeSprite(sprite.getId());
        }

        System.out.println("Drawing state at tick: " + currentTick);
        State currentState = this.states.get(currentTick);
        for (UserState userState : currentState.getUserStates()) {
            this.drawUserState(userState);
        }

        if (this.onVisualizationStateChangedListener != null) {
            this.onVisualizationStateChangedListener.onTick(this.currentTick, currentState.getActiveAgents());
        }

    }

    private void drawUserState(UserState userState) {
        Sprite sprite = createSpriteIfDoesNotExist(userState);
        if (sprite == null) {
            return;
        }
        String spriteCurrentEdge = sprite.getAttribute("currentEdge");
        String stateCurrentEdge = userState.getEdgeId();

        System.out.println("State edge:" + stateCurrentEdge);
        if (!spriteCurrentEdge.equals(stateCurrentEdge)) {
            System.out.println("Edges do not match");
            sprite.detach();
            sprite.attachToEdge(userState.getEdgeId());
        }

        sprite.setPosition(userState.getProgress());

        if (userState.isData()) {
            sprite.addAttribute("ui.class", "active");
        }
    }

    private Sprite createSpriteIfDoesNotExist(UserState userState) {
        Sprite sprite = this.spriteManager.getSprite("" + userState.getPersonId());
        if (sprite == null) {
            if (graph.getEdge(userState.getEdgeId()) == null) {
                System.out.println("Edge does not exist.");
                return null;
            }
            System.out.println("Creating sprite...");
            sprite = this.spriteManager.addSprite("" + userState.getPersonId());
            sprite.attachToEdge(userState.getEdgeId());
            sprite.addAttribute("currentEdge", userState.getEdgeId());
            sprite.setPosition(0);
        }
        return sprite;
    }


    public void toggleSimulation() {
        this.isRunning = !this.isRunning;
    }

    public void restart() {
        this.currentTick = 0;
        this.isRunning = true;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
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

    public OnVisualizationStateChangedListener getOnVisualizationStateChangedListener() {
        return onVisualizationStateChangedListener;
    }

    public void setOnVisualizationStateChangedListener(OnVisualizationStateChangedListener onVisualizationStateChangedListener) {
        this.onVisualizationStateChangedListener = onVisualizationStateChangedListener;
    }
}
