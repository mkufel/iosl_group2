import visualization.services.VisualizationEngine;
import visualization.views.MapView;

import java.util.Timer;

public class App {
    public static void main(String[] argv) {

        MapView mapView = new MapView();

        Timer timer = new Timer(true);
        VisualizationEngine vis = new VisualizationEngine(mapView.showGraph());

        timer.scheduleAtFixedRate(vis, 0, 1000);
    }
}
