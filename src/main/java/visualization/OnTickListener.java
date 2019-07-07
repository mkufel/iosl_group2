package visualization;
/**
 * Callback to call every tick.
 */
public interface OnTickListener {
    void onTick(int tick, int allAgents, int activeAgents, double disseminationFactor);
}
