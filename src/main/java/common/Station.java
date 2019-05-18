package common;

import lombok.Data;

@Data
public class Station {
    private String id;

    private String name;

    private GeoCoords location;

    private int population;

    public Station(String id) {
        this.id = id;
    }

    public Station(String id, String name, GeoCoords location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(GeoCoords location) {
        this.location = location;
    }
}
