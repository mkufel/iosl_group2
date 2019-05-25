package init;

import com.opencsv.CSVReader;
import common.GeoCoords;
import common.Line;
import common.ScheduleItem;
import common.Station;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class InitEngine {


    public InitEngine() {}


    public static void main(String[] args) {

        InitEngine ob = new InitEngine();
        Map<String, String> routeIdsToLineNames = ob.readRoutesFromCSV("resources/routes.csv");
        Map<String, ArrayList<String>> routesToTrips = ob.mapRoutesToTripsFromCSV(routeIdsToLineNames, "resources/trips.csv");
        Map<String, ArrayList<Station>> tripsToStations = ob.parseStationTimesFromCSV("resources/stop_times.csv");
        Map<String, ArrayList<Station>> routesToStations = ob.mapRouteToStations(routesToTrips, tripsToStations);
        Map<String, ArrayList<Station>> routeIdsToStations = ob.addStationCoordsToRouteStationsMapping(routesToStations, "resources/stops.csv");

        ob.createMap(routeIdsToStations, routeIdsToLineNames);

        Map<String, ArrayList<ScheduleItem>> mapStopsToScheduleItems = ob.mapStopsToScheduleItems("resources/stop_times.csv", routesToTrips, routeIdsToLineNames);

    }

    /**
     * Parse the CSV file mapping ubahn lines to route identifiers
     * @param fileName input file "routes.csv"
     * @return Dictionary route_id -> ubahn_line_name
     */
    private Map<String, String> readRoutesFromCSV(String fileName) {
        Map<String, String> routes_dict = new HashMap<>();
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
            ) {

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                // Select records that start with
                if (nextRecord[2].startsWith("U")) {
                    // Store the route id as a key and a corresponding line name as a value
                    routes_dict.put(nextRecord[0], nextRecord[2]);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return routes_dict;
    }

    /**
     * Create a mapping of route_id to trips at a given route from trips.csv
     * @param routes_dict Dictionary route_id -> ubahn_line_name
     * @param fileName path to trips.csv
     * @return Hashmap, key:route_id, value: ArrayList<trip_id>
     */
    private Map<String, ArrayList<String>> mapRoutesToTripsFromCSV(Map<String, String> routes_dict, String fileName) {
        Map<String, ArrayList<String>> routesToTrips_dict = new HashMap<>();
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                String route_id = nextRecord[0];
                String trip_id = nextRecord[2];
                ArrayList<String> currentTrips;

                // If a route corresponding to a parsed trip is included in the routes_dict (is an u-bahn path)
                if (routes_dict.containsKey(route_id)) {

                    // If the route_id - List<Trips> mapping already exists, append the current trip_id. Else create a new entry
                    if (routesToTrips_dict.containsKey(route_id)) {
                        currentTrips = routesToTrips_dict.get(route_id);
                        currentTrips.add(trip_id);
                        routesToTrips_dict.put(route_id, currentTrips);
                    } else {
                        routesToTrips_dict.put(route_id, new ArrayList<String>(Arrays.asList(trip_id)));
                    }
                }
            }
        } catch (Exception e) {e.printStackTrace(); }

        return routesToTrips_dict;
    }

    /**
     * Parse stop_times.csv to create a mapping trip_id - ArrayList<station_id>
     * @param fileName path to stop_times.csv
     * @return mapping trip_id - ArrayList<station_id>
     */
    private Map<String, ArrayList<Station>> parseStationTimesFromCSV(String fileName) {
        Path pathToFile = Paths.get(fileName);
        Map<String, ArrayList<Station>> tripsToStations_dict = new HashMap<>();

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1)
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                try{
                    String trip_id = nextRecord[0];
                    long station_id = Long.parseLong(nextRecord[3]);
                    ArrayList<Station> currentStations;

                    // If the trip_id - List<Station> already exists, append the current station_id. Else create a new entry
                    if (tripsToStations_dict.containsKey(trip_id)) {
                        currentStations = tripsToStations_dict.get(trip_id);
                        currentStations.add(new Station(station_id));
                        tripsToStations_dict.put(trip_id, currentStations);
                    } else {
                        tripsToStations_dict.put(trip_id, new ArrayList<Station>(Arrays.asList(new Station(station_id))));
                    }
                } catch (NumberFormatException ex){
                    System.out.println("Skipped station with id: " + nextRecord[3]);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }

        return tripsToStations_dict;
    }


    /**
     * Create a mapping of route_id - List<station_id> </station_id>based on the mappings of route_id - List<trip_id>
     * and trip_id - List<station_id>
     * @param routesToTrips_dict mapping route_id - List<trip_id>
     * @param tripsToStations_dict mapping trip_id - List<station_id>
     * @return mapping route_id - List<station_id>
     */
    private Map<String, ArrayList<Station>> mapRouteToStations (Map<String, ArrayList<String>> routesToTrips_dict, Map<String, ArrayList<Station>> tripsToStations_dict) {
        Map<String, ArrayList<Station>> routesToStations = new HashMap<>();

        for (String key : routesToTrips_dict.keySet()) {
            int max = 0;
            String maxTripId = "";
            // Loop through all trips for the current key(route) and select one with the largest number of stations.
            // Trips do no regularly go through all stations on a route. Crucial to get all stations of the path for
            // visualization of a connection map.
            for (String trip_id : routesToTrips_dict.get(key)) {
                int temp = tripsToStations_dict.get(trip_id).size();
                if (temp > max) {
                    max = temp;
                    maxTripId = trip_id;
                }
            }

            routesToStations.put(key, tripsToStations_dict.get(maxTripId));
        }

        return routesToStations;
    }

    /**
     * Based on the mapping of route_id - List<station_id>, parse the stops.csv and
     * return a mapping of route_id - List<Station> that includes geo coordinates of each station.
     * @param routesToStations mapping of route_id - List<station_id>
     * @param fileName path to stops.csv
     * @return mapping of route_id - List<Station>
     */
    private Map<String, ArrayList<Station>> addStationCoordsToRouteStationsMapping (Map<String, ArrayList<Station>> routesToStations, String fileName) {
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                try{
                    Double stop_lat = Double.parseDouble(nextRecord[4]);
                    Double stop_lon = Double.parseDouble(nextRecord[5]);
                    String name = nextRecord[2];

                    for (String key : routesToStations.keySet()) {
                        ArrayList<Station> stations = routesToStations.get(key);

                        for (int i = 0; i < stations.size(); i++) {
                            Station st = stations.get(i);

                            if (Long.valueOf(st.getId()).equals(Long.parseLong(nextRecord[0]))) {

                                st.setName(name);
                                st.setLocation(new GeoCoords(stop_lat, stop_lon));
                            }
                        }
                    }
                } catch (NumberFormatException ex){
                    System.out.println("Skipped station with id: " + nextRecord[0]);
                }
            }
        } catch (Exception e) { e.printStackTrace();}

        return routesToStations;
    }


    private common.Map createMap (Map<String, ArrayList<Station>> routeIdsToStations, Map<String, String> routes_dict) {

        common.Map map = new common.Map();
        ArrayList<Line> lines = new ArrayList<>();
        for (String key : routeIdsToStations.keySet()){
            String lineName = routes_dict.get(key);
            Line line = new Line();
            line.setName(lineName);
            line.setStations(routeIdsToStations.get(key));
            lines.add(line);
        }

        map.setLines(lines);
        return  map;
    }


    private Map<String, ArrayList<ScheduleItem>> mapStopsToScheduleItems (String fileName, Map<String, ArrayList<String>> routeIdsToTrips, Map<String, String> routesIdsToLineNames) {
        Map<String, ArrayList<ScheduleItem>> stopsToScheduleItems = new HashMap<>();
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader,  ',', '"', 1);
        ) {
            String[] nextRecord = csvReader.readNext();
            String[] nextNextRecord = csvReader.readNext();
            String line_name = "";

            // Read the first entry in the file
            if ((nextRecord = csvReader.readNext()) != null) {

                // Read the proceeding entry
                if ((nextNextRecord = csvReader.readNext()) != null) {

                    // If extracted line_name based on the trip_id, then it's an ubahn line, update stopToScheduleItems mapping
                    if (!(line_name = getLineNameFromTripId(nextRecord[0], routeIdsToTrips, routesIdsToLineNames)).equals("")) {
                        stopsToScheduleItems = updateStopsToScheduleItemsMap(stopsToScheduleItems, nextRecord, nextNextRecord, line_name);
                    }

                    nextRecord = nextNextRecord;
                }
            }

            // Try to read the nextNextRecord consecutive to nextRecord
            while ((nextNextRecord = csvReader.readNext()) != null) {

                // If extracted line_name based on the trip_id, then it's an ubahn line, update stopToScheduleItems mapping
                if (!(line_name = getLineNameFromTripId(nextRecord[0], routeIdsToTrips, routesIdsToLineNames)).equals("")) {
                    stopsToScheduleItems = updateStopsToScheduleItemsMap(stopsToScheduleItems, nextRecord, nextNextRecord, line_name);
                }

                nextRecord = nextNextRecord;
            }
        } catch (Exception e) {e.printStackTrace(); }

        return  stopsToScheduleItems;
    }


    private Map<String, ArrayList<ScheduleItem>> updateStopsToScheduleItemsMap (Map<String, ArrayList<ScheduleItem>> stopsToScheduleItems, String[] nextRecord, String[] nextNextRecord, String line_name) {

        // If both entries belong to the same trip [0] and are consecutive in the stop sequence [4],
        // extract the line name and if U-bahn create an entry in stopsToScheduleItem
        if (nextRecord[0].equals(nextNextRecord[0]) && Integer.parseInt(nextRecord[4]) + 1 == Integer.parseInt(nextNextRecord[4])) {

            ArrayList<ScheduleItem> tempScheduleItems;
            String departure_time = nextRecord[2];
            String stop_id = nextRecord[3];
            String nextStop_id = nextNextRecord[3];

            // Remove preceeding 0's from stop ids
            if (stop_id.startsWith("0")) stop_id = stop_id.substring(1, stop_id.length());
            if (nextStop_id.startsWith("0")) nextStop_id = nextStop_id.substring(1, stop_id.length());

            // If a stop is already in the mapping, extract the ScheduleItems and append a new one
            if ((tempScheduleItems = stopsToScheduleItems.get(stop_id)) != null) {
                tempScheduleItems.add(new ScheduleItem(line_name, departure_time, nextStop_id));
                stopsToScheduleItems.put(stop_id, tempScheduleItems);
            }
            // Else create a new entry Key: stop_id, Value: ArrayList<ScheduleItem>
            else {
                tempScheduleItems = new ArrayList<>();
                tempScheduleItems.add(new ScheduleItem(line_name, departure_time, nextStop_id));
                stopsToScheduleItems.put(stop_id, tempScheduleItems);
            }
        }

        return stopsToScheduleItems;
    }


    private String getLineNameFromTripId(String trip_id, Map<String, ArrayList<String>> routeIdsToTrips, Map<String, String> routesIdsToLineNames) {
        String line_name = "";

        // Check if a given trip_id is included in a list of trips for a given route.
        // If so extract the line name corresponding to the route_id and set the variable
        for (String key : routeIdsToTrips.keySet()) {
            if (routeIdsToTrips.get(key).contains(trip_id)) {
                line_name = routesIdsToLineNames.get(key);
                return line_name;
            }
        }

        return line_name;
    }
}
