package common;

import lombok.Data;

@Data
public class GeoCoords {
    private double lon;
    private double lat;

    public GeoCoords(double lat, double lon) {
        this.lon = lon;
        this.lat = lat;
    }
}
