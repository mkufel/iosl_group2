package common;

import java.util.List;

/**
 * Metro line.
 */
public class Line {
    // Name of the line, e.g. 'U9'
    private String name;

    // Stations that are part of this line
    // Note that a station can be part of several lines
    private List<Station> stations;

    public Line(String name, List<Station> stations) {
        this.name = name;
        this.stations = stations;
    }

    public Line() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Station> getStations() {
        return stations;
    }

    public void setStations(List<Station> stations) {
        this.stations = stations;
    }
}
