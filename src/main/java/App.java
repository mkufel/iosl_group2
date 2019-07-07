
import TraceGenerationEngine.TraceGenerationEngine;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import common.Map;
import common.State;
import dissemination.DisseminationEngine;
import init.InitEngine;
import visualization.VisualizationEngine;
import visualization.VisualizationWindow;
import visualization.services.Map2GraphConverter;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.xml.crypto.KeySelector;
import java.io.File;
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
        serializeStatesToJSON(states);

//        byte[] jsonData = Files.readAllBytes(Paths.get("resources/states.json"));
//        List<State> statesFromJson = Arrays.asList(objectMapper.readValue(jsonData, State[].class));

        VisualizationEngine visualizationEngine = new VisualizationEngine(Map2GraphConverter.convert(berlinMap), states);
        VisualizationWindow visualizationWindow = new VisualizationWindow(visualizationEngine, traceEngine);

        visualizationWindow.setOnReloadListener(() -> {
            disseminationEngine.setStates(traceEngine.getStates());
            List<State> newStates = disseminationEngine.calculateDissemination();
            visualizationEngine.setRunning(false);
            visualizationEngine.setStates(newStates);
            visualizationEngine.restart();
        });

        System.out.println("Drawing user states on the map...");

        visualizationEngine.setRunning(true);
        visualizationWindow.setVisible(true);

        // Controls how fast ticks should change in the visualization
        timer.scheduleAtFixedRate(visualizationEngine, 0, 1000);
    }

    /**
     * Serializes a list of states to JSON for efficient visualization
     * @param states a list of user states, input to the visualization engine
     */
    private static void serializeStatesToJSON(List<State> states) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            //Convert object to JSON string and save into file directly
            objectMapper.writeValue(new File("resources/states.json"), states);

            //Convert object to JSON string
            String jsonInString = objectMapper.writeValueAsString(states);

            //Convert object to JSON string and pretty print
            jsonInString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(states);
        } catch (Exception e) {
            System.out.println("Failed to parse the list of states to JSON ");
            e.printStackTrace();
        }
    }
}
