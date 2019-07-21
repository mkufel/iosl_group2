
import com.fasterxml.jackson.databind.ObjectMapper;
import common.Map;
import common.State;
import dissemination.DisseminationEngine;
import parser.ParserEngine;
import trace.TraceGenerationEngine;
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
        // Change to a better renderer.
        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");


        // Parse the input files
        ParserEngine parserEngine = new ParserEngine();
        Map berlinMap = parserEngine.createMapFromBVGFiles();

        // Genereate user traces
        TraceGenerationEngine traceEngine = new TraceGenerationEngine(parserEngine);

        // Add dissemination information to the generated states
        DisseminationEngine disseminationEngine = new DisseminationEngine(traceEngine.getStates());
        List<State> states = disseminationEngine.calculateDissemination();

        // Create the visualization from the states
        VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), states);
        VisualizationWindow visualizationWindow = new VisualizationWindow(visualizationEngine, traceEngine);

        // Set listeners for global reload events
        visualizationWindow.setOnReloadListener((loadFromFileName) -> {
            if (loadFromFileName == null) {
                disseminationEngine.setStates(traceEngine.getStates());
                List<State> newStates = disseminationEngine.calculateDissemination();
                visualizationEngine.setRunning(false);
                visualizationEngine.setStates(newStates);
                visualizationEngine.restart();
            } else {
                // This can run out of memory if the loaded file is too big!
                byte[] jsonData = Files.readAllBytes(Paths.get(loadFromFileName));
                ObjectMapper objectMapper = new ObjectMapper();
                List<State> statesFromJson = Arrays.asList(objectMapper.readValue(jsonData, State[].class));
                visualizationEngine.setRunning(false);
                visualizationEngine.setStates(statesFromJson);
                visualizationEngine.restart();
            }
        });

        System.out.println("Drawing user states on the map...");

        // Start the visualization engine
        visualizationEngine.setRunning(true);
        visualizationWindow.setVisible(true);

        // Controls how fast ticks should change in the visualization
        Timer timer = new Timer(true);
        timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
    }

}
