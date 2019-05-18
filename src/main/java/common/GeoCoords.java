package common;

import lombok.Data;

@Data
public class GeoCoords {
    private double lat;
    private double lon;

    public GeoCoords(double lat, double lon) {
        this.lat = lat;
        this.lon = lon;
    }
}
