package visualization.services;

import common.GeoCoords;
import common.Line;
import common.Map;
import common.Station;

import java.util.Arrays;

public class MapFactory {
    Map createMap() {
        Station station1 = new Station(
                "1",
                "Station 1",
                new GeoCoords(52.5115820, 13.3225810),
                100);

        Station station2 = new Station(
                "2",
                "Station 2",
                new GeoCoords(52.5068820, 13.3329270),
                100);

        Station station3 = new Station(
                "3",
                "Station 3",
                new GeoCoords(52.5037630, 13.3314190),
                100);

        Line line = new Line();
        line.setName("Line");
        line.setStations(Arrays.asList(station1, station2, station3));

        Map map = new Map();
        map.setLines(Arrays.asList(line));

        return map;
    }
}
