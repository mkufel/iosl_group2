package common;

import java.util.List;

public class Line {
    private String name;

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
