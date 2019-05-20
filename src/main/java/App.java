import common.Map;
import common.State;
import visualization.Window;
import visualization.services.MapFactory;
import visualization.services.StateFactory;
import visualization.VisualizationEngine;
import visualization.services.Map2GraphConverter;

import java.util.List;
import java.util.Timer;

public class App {

    public static void main(String[] argv) {
        System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Map map = MapFactory.createMap();
        List<State> states = StateFactory.createStates();

        Timer timer = new Timer(true);
        VisualizationEngine vis = new VisualizationEngine(Map2GraphConverter.convert(map), states);

        Window window = new Window("Fancy title :]", vis.getViewPanel());
        window.setVisible(true);

        timer.scheduleAtFixedRate(vis, 0, 1000);
    }
}
