import visualization.Window;
import visualization.services.StateFactory;
import visualization.VisualizationEngine;
import visualization.views.MapView;

import java.util.Timer;

public class App {

    public static void main(String[] argv) {
//        MapView mapView = new MapView();
//
//        Timer timer = new Timer(true);
//        VisualizationEngine vis = new VisualizationEngine(mapView.showGraph(), new StateFactory().createStates());
//
//        timer.scheduleAtFixedRate(vis, 0, 1000);

        Window window = new Window("Fancy title :]");
        window.setVisible(true);
    }
}
