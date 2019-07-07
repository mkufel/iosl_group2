package visualization;

public interface OnVisualizationStateChangedListener {
    void onTick(int tick, int allAgents, int activeAgents, double disseminationFactor);
}
