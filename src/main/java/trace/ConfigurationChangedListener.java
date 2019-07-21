package trace;

/**
 * Called when the configuration is changed in the GUI.
 */
public interface ConfigurationChangedListener {
    void onConfigurationChanged(int totalPopulation, int totalTicks);
}
