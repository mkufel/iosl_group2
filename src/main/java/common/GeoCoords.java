package common;

/**
 * Geographical point.
 *
 * Represented as a pair of [lon, lat] coordinates.
 */
public class GeoCoords {
    private double lon;
    private double lat;

    public GeoCoords(double lat, double lon) {
        this.lon = lon;
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
}
