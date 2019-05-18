package common;

import lombok.Data;

@Data
public class Station {
    private String id;

    private String name;

    private GeoCoords location;

    private int population;

    public Station(String id, String name, GeoCoords location, int population) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.population = population;
    }
}
