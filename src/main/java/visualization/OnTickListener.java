package visualization;

/**
 * Callback to call every tick.
 */
public interface OnTickListener {
    void onTick(int tick, int activeAgents, double disseminationFactor);
}
