package visualization;

import common.State;
import common.Station;
import common.UserState;
import org.graphstream.graph.Graph;
import org.graphstream.ui.spriteManager.Sprite;
import org.graphstream.ui.spriteManager.SpriteManager;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Responsible for visualizing States on a GraphStream graph.
 */
public class VisualizationEngine extends TimerTask {

    private int currentTick = 0;
    private boolean isRunning = false;

    private Graph graph;
    private ViewPanel viewPanel;
    private SpriteManager spriteManager;

    private List<State> states;

    private OnTickListener onTickListener;

    /**
     * Initializes the visualization engine with the given graph and states
     *
     * @param graph  The graph to show
     * @param states The states to visualize
     */
    public VisualizationEngine(Graph graph, List<State> states) {
        this.graph = graph;

        Viewer viewer = new Viewer(this.graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.disableAutoLayout();
        viewer.enableXYZfeedback(true);

        this.viewPanel = viewer.addDefaultView(false);
        this.spriteManager = new SpriteManager(graph);
        this.states = states;
    }

    /**
     * Gets the ViewPanel for display
     *
     * @return The ViewPanel object
     */
    public ViewPanel getViewPanel() {
        return this.viewPanel;
    }

    /**
     * Starts the visualization loop
     */
    @Override
    public void run() {
        if (this.isRunning) {
            drawCurrentState();
            currentTick += 1;
        }
    }

    /**
     * Statelessly draws the current state based on the States array and the current tick.
     */
    private void drawCurrentState() {
        // Check if state exists
        if (currentTick > this.states.size() - 1 || currentTick < 0) {
            this.isRunning = false;
            return;
        }

        removeOldSpritesFromGraph();

        State currentState = this.states.get(currentTick);
        for (UserState userState : currentState.getUserStates()) {
            this.drawUserState(userState);
        }

        if (this.onTickListener != null) {
            this.onTickListener
                    .onTick(this.currentTick, currentState.getUserStates().size(), currentState.getActiveAgents(), currentState.getDisseminationFactor());
        }

    }

    /**
     * Removes sprites that are currently visible on the graph.
     */
    private void removeOldSpritesFromGraph() {
        List<Sprite> toRemoveSprites = new ArrayList<>();
        this.spriteManager.iterator().forEachRemaining(toRemoveSprites::add);

        for (Sprite sprite : toRemoveSprites) {
            this.spriteManager.removeSprite(sprite.getId());
        }
    }

    /**
     * Draws a given UserState on the graph
     *
     * @param userState The UserState to visualize on the map
     */
    private void drawUserState(UserState userState) {
        Sprite sprite = createSpriteIfDoesNotExist(userState);

        if (sprite == null) {
            return;
        }

        String spriteCurrentEdge = sprite.getAttribute("currentEdge");
        String stateCurrentEdge = userState.getEdgeId();

        if (!spriteCurrentEdge.equals(stateCurrentEdge)) {
            if (!userState.getStationStart().equals(userState.getStationEnd())) {
                System.out.println("Edges do not match");
                sprite.detach();
                sprite.attachToEdge(userState.getEdgeId());

            } else if (userState.getStationStart().equals(userState.getStationEnd())) {
                sprite.detach();
                sprite.attachToNode("" + userState.getStationEnd());
            }
        }

        if (!userState.getStationStart().equals(userState.getStationEnd())) {
            sprite.setPosition(userState.getProgress());
        }

        if (userState.hasData()) {
            sprite.addAttribute("ui.class", "active");
        }
    }

    /**
     * Creates a sprite on the graph if it does not already exist.
     *
     * @param userState The UserState which the sprite will be created of.
     * @return The found or created sprite
     */
    private Sprite createSpriteIfDoesNotExist(UserState userState) {
        Sprite sprite = this.spriteManager.getSprite("" + userState.getPersonId());

        if (sprite == null) {
            if (graph.getEdge(userState.getEdgeId()) == null) {
                if (userState.getStationStart().equals(userState.getStationEnd())) {
                    sprite = this.spriteManager.addSprite("" + userState.getPersonId());
                    sprite.attachToNode("" + userState.getStationEnd());
                    sprite.addAttribute("currentEdge", userState.getEdgeId());

                    return sprite;
                }

                System.out.println("Edge " + userState.getStationStart() + "_" + userState.getStationEnd() + " does not exist.");

                return null; // Edge not found.
            }

            sprite = this.spriteManager.addSprite("" + userState.getPersonId());
            sprite.attachToEdge(userState.getEdgeId());
            sprite.addAttribute("currentEdge", userState.getEdgeId());
            sprite.setPosition(0);
        }

        return sprite;
    }

    /**
     * Turns the simulation on or off.
     */
    public void toggleSimulation() {
        this.isRunning = !this.isRunning;
    }

    /**
     * Restarts the simulation from tick 0.
     */
    public void restart() {
        this.currentTick = 0;
        this.isRunning = true;
    }

    /**
     * Returns the current tick in the visualization.
     */
    public int getCurrentTick() {
        return currentTick;
    }

    /**
     * Sets the current tick and forces a re-draw of the simulation.
     *
     * @param currentTick The tick to set.
     */
    public void setCurrentTick(int currentTick) {
        this.currentTick = currentTick;

        // If the simulation is not running we should visualize manual movement of states/ticks
        if (!isRunning) {
            this.drawCurrentState();
        }
    }

    /**
     * Returns whether the simulation is running.
     */
    public boolean isRunning() {
        return isRunning;
    }

    /**
     * Sets the simulation to run or not.
     */
    public void setRunning(boolean running) {
        isRunning = running;
    }

    /**
     * Returns the listener for Visualization state changes.
     */
    public OnTickListener getOnTickListener() {
        return onTickListener;
    }

    /**
     * Sets the listener for visualization state changes.
     *
     * @param onTickListener The listener interface.
     */
    public void setOnTickListener(OnTickListener onTickListener) {
        this.onTickListener = onTickListener;
    }

    /**
     * Sets the states to visualize.
     */
    public void setStates(List<State> states) {
        this.states = states;
    }

    public double getTotalNumberOfUsersWithData() {
        ArrayList<UserState> userStates = new ArrayList<>();

        states.stream()
            .forEach(state -> state.getUserStates().stream()
                .filter(UserState::hasData)
                .forEach(userStateWithData -> userStates.add(userStateWithData)));

        return userStates.stream()
                .filter(distinctByKey(UserState::getPersonId))
                .count();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
