<<<<<<< Updated upstream
import common.Map;
import common.State;
import init.InitEngine;
import visualization.VisualizationEngine;
import visualization.Window;
import visualization.services.Map2GraphConverter;
import visualization.services.MapFactory;
import visualization.services.StateFactory;
=======
import visualization.services.StateFactory;
import visualization.services.VisualizationEngine;
import visualization.views.MapView;
>>>>>>> Stashed changes

import java.util.List;
import java.util.Timer;

public class App {

    public static void main(String[] argv) {
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");


        InitEngine initEngine = new InitEngine();
        Map berlinMap = initEngine.createMapFromBVGFiles();

        Map map = MapFactory.createMap();
        List<State> states = StateFactory.createStates();

        Timer timer = new Timer(true);
<<<<<<< Updated upstream
        VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), states);
        visualizationEngine.setRunning(true);
        Window window = new Window("Dissemination Simulation", visualizationEngine.getViewPanel(), visualizationEngine);
        window.setVisible(true);
=======
        VisualizationEngine vis = new VisualizationEngine(mapView.showGraph(), new StateFactory().createStates());
>>>>>>> Stashed changes

        timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
    }
}
