
import TraceGenerationEngine.TraceGenerationEngine;
import common.Map;
import common.State;
import dissemination.DisseminationEngine;
import init.InitEngine;
import visualization.VisualizationEngine;
import visualization.VisualizationWindow;
import visualization.services.Map2GraphConverter;

import java.io.IOException;
import java.util.List;
import java.util.Timer;

public class App {

    public static void main(String[] argv) {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Timer timer = new Timer(true);

        try {
            InitEngine initEngine = new InitEngine();
            Map berlinMap = initEngine.createMapFromBVGFiles();
            TraceGenerationEngine traceEngine = new TraceGenerationEngine(initEngine);

            DisseminationEngine disseminationEngine = new DisseminationEngine(traceEngine.getStates());
            List<State> states = disseminationEngine.calculateDissemination();

            VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), states);
            VisualizationWindow visualizationWindow = new VisualizationWindow(visualizationEngine, traceEngine);

            visualizationWindow.setOnSimulationReloadListener(() -> {
                disseminationEngine.setStates(traceEngine.getStates());
                List<State> newStates = disseminationEngine.calculateDissemination();
                visualizationEngine.setRunning(false);
                visualizationEngine.setStates(newStates);
                visualizationEngine.restart();
            });

            System.out.println("Drawing user states on the map...");
            visualizationEngine.setRunning(true);
            visualizationWindow.setVisible(true);

            // TODO Encapsulate timer in one of the visualization classes
            timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
