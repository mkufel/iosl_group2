package common;

import lombok.Data;

@Data
public class Station {
    private String id;

    private String name;

    private GeoCoords location;

    private int population;
}
