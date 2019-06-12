package visualization;

public interface OnVisualizationStateChangedListener {
    void onTick(int tick, int activeAgents, double disseminationFactor);
}
