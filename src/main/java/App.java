
import TraceGenerationEngine.TraceGenerationEngine;
import common.Map;
import common.State;
import init.InitEngine;
import visualization.VisualizationEngine;
import visualization.VisualizationWindow;
import visualization.services.Map2GraphConverter;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

public class App {

    public static void main(String[] argv) {
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Timer timer = new Timer(true);

        Map berlinMap = new InitEngine().createMapFromBVGFiles();

        try {
            TraceGenerationEngine engine = new TraceGenerationEngine();
            List<State> statesBerlin = engine.getStates();

            VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), statesBerlin);
            VisualizationWindow visualizationWindow = new VisualizationWindow(visualizationEngine.getViewPanel(), visualizationEngine, engine);

            visualizationEngine.setRunning(true);
            visualizationWindow.setVisible(true);

            timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
