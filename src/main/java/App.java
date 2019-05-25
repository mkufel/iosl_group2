
import TraceGenerationEngine.TraceGenerationEngine;
import common.Map;
import common.State;
import init.InitEngine;
import visualization.VisualizationEngine;
import visualization.Window;
import visualization.services.Map2GraphConverter;
import visualization.services.MapFactory;
import visualization.services.StateFactory;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

public class App {

    public static void main(String[] argv) {
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");


        InitEngine initEngine = new InitEngine();
        Map berlinMap = initEngine.createMapFromBVGFiles();

        Map map = MapFactory.createMap();
        List<State> states = StateFactory.createStates();
        try {
            TraceGenerationEngine engine = new TraceGenerationEngine();
            List<State> statesBerlin = engine.getStates();
            Timer timer = new Timer(true);
            VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), statesBerlin);
            visualizationEngine.setRunning(true);
            Window window = new Window("Dissemination Simulation", visualizationEngine.getViewPanel(), visualizationEngine, engine);
            window.setVisible(true);

            timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }
}
