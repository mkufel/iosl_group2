
import trace.TraceGenerationEngine;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Map;
import common.State;
import dissemination.DisseminationEngine;
import init.InitEngine;
import visualization.VisualizationEngine;
import visualization.VisualizationWindow;
import visualization.services.Map2GraphConverter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

public class App {
    public static void main(String[] argv) throws IOException {
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Timer timer = new Timer(true);

        InitEngine initEngine = new InitEngine();
        Map berlinMap = initEngine.createMapFromBVGFiles();

        TraceGenerationEngine traceEngine = new TraceGenerationEngine(initEngine);
        DisseminationEngine disseminationEngine = new DisseminationEngine(traceEngine.getStates());

        List<State> states = disseminationEngine.calculateDissemination();


        VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), states);
        VisualizationWindow visualizationWindow = new VisualizationWindow(visualizationEngine, traceEngine);

        visualizationWindow.setOnReloadListener((loadFromFileName) -> {
            if (loadFromFileName == null) {
                disseminationEngine.setStates(traceEngine.getStates());
                List<State> newStates = disseminationEngine.calculateDissemination();
                visualizationEngine.setRunning(false);
                visualizationEngine.setStates(newStates);
                visualizationEngine.restart();
            } else {
                byte[] jsonData = Files.readAllBytes(Paths.get(loadFromFileName));
                ObjectMapper objectMapper = new ObjectMapper();
                List<State> statesFromJson = Arrays.asList(objectMapper.readValue(jsonData, State[].class));
                visualizationEngine.setRunning(false);
                visualizationEngine.setStates(statesFromJson);
                visualizationEngine.restart();
            }
        });

        System.out.println("Drawing user states on the map...");

        visualizationEngine.setRunning(true);
        visualizationWindow.setVisible(true);

        // Controls how fast ticks should change in the visualization
        timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
    }

}
