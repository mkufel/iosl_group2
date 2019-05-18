package visualization.services;

import common.GeoCoords;

public class GeoCoordsUtils {
    private static final double MIN_LON = 11.0;
    private static final double MAX_LON = 15.0;

    private static final double MIN_LAT = 50.0;
    private static final double MAX_LAT = 55.0;

    private static int latToX(double lat) {
//        System.out.println((lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * 100 * 1000);

        return (int) ((lat - MIN_LAT) / (MAX_LAT - MIN_LAT) * 100 * 1000);
    }

    private static int lonToY(double lon) {
//        System.out.println((lon - MIN_LON) / (MAX_LON - MIN_LON) * 100 * 1000);

        return (int) ((lon - MIN_LON) / (MAX_LON - MIN_LON) * 100 * 1000);
    }

    /**
     * Converts geographical coordinates into screen coordinates in a range [0, 100].
     *
     * Returns [Y,X]
     */
    public static int[] convertToCartesian(GeoCoords coords) {
        int[] res = new int[2];

        res[1] = latToX(coords.getLat());
        res[0] = lonToY(coords.getLon());

        return res;
    }
}
