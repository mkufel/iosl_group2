package trace;

import com.opencsv.CSVReader;
import common.GeoCoords;
import common.Line;
import common.Station;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class TraceEngine {


    public TraceEngine() {}


    public static void main(String[] args) {
        TraceEngine ob = new TraceEngine();
        Map<String, String> routes_dict = ob.readRoutesFromCSV("resources/routes.csv");
        Map<String, ArrayList<String>> routesToTrips = ob.mapRoutesToTripsFromCSV(routes_dict, "resources/trips.csv");
        Map<String, ArrayList<Station>> tripsToStations = ob.parseStationTimesFromCSV("resources/stop_times.csv");
        Map<String, ArrayList<Station>> routesToStations = ob.mapRouteToStations(routesToTrips, tripsToStations);
        Map<String, ArrayList<Station>> routeIdsToStations = ob.parseStations(routesToStations, "resources/stops.csv");

        ob.createMap(routeIdsToStations, routes_dict);


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
                CSVReader csvReader = new CSVReader(reader);
            ) {

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                // Select records that start with
                if (nextRecord[2].startsWith("U")) {
                    // Store the route id as a key and a corresponding line name as a value
                    routes_dict.put(nextRecord[0], nextRecord[2]);
                }
            }
        } catch (Exception e) {}

        return routes_dict;
    }

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

                if (routes_dict.containsKey(route_id)) {
                    if (routesToTrips_dict.containsKey(route_id)) {
                        ArrayList<String> currentTrips = routesToTrips_dict.get(route_id);
                        currentTrips.add(trip_id);
                        routesToTrips_dict.put(route_id, currentTrips);
                    } else {
                        routesToTrips_dict.put(route_id, new ArrayList<String>(Arrays.asList(trip_id)));
                    }
                }
            }
        } catch (Exception e) {}

        return routesToTrips_dict;
    }

    private Map<String, ArrayList<Station>> parseStationTimesFromCSV(String fileName) {
        Path pathToFile = Paths.get(fileName);
        Map<String, ArrayList<Station>> tripsToStations_dict = new HashMap<>();

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                String trip_id = nextRecord[0];
                String station_id = nextRecord[3];

                if (tripsToStations_dict.containsKey(trip_id)) {
                    ArrayList<Station> currentStations = tripsToStations_dict.get(trip_id);
                    currentStations.add(new Station(station_id));
                    tripsToStations_dict.put(trip_id, currentStations);
                }
                else {
                   tripsToStations_dict.put(trip_id, new ArrayList<Station>(Arrays.asList(new Station(station_id))));
                }
            }
        } catch (Exception e) {}

        return tripsToStations_dict;
    }


    private Map<String, ArrayList<Station>> mapRouteToStations (Map<String, ArrayList<String>> routesToTrips_dict, Map<String, ArrayList<Station>> tripsToStations_dict) {
        Map<String, ArrayList<Station>> routesToStations = new HashMap<>();

        for (String key : routesToTrips_dict.keySet()) {

            int max = 0;
            String maxTripId = "";
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

    private Map<String, ArrayList<Station>> parseStations (Map<String, ArrayList<Station>> routesToStations, String fileName) {
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
        ) {

            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {

                Double stop_lat = Double.parseDouble(nextRecord[4]);
                Double stop_lon = Double.parseDouble(nextRecord[5]);
                String name = nextRecord[2];

                for (String key : routesToStations.keySet()) {
                    ArrayList<Station> stations = routesToStations.get(key);

                    for (int i = 0; i < stations.size(); i++) {
                        Station st = stations.get(i);
                        if (st.getId().contains(nextRecord[0])) {

                            st.setName(name);
                            st.setLocation(new GeoCoords(stop_lat, stop_lon));
                        }
                    }
                }

            }
        } catch (Exception e) { System.out.println(e);}

        return routesToStations;
    }

    private Map createMap (Map<String, ArrayList<Station>> routeIdsToStations, Map<String, String> routes_dict) {

        for (String key : routeIdsToStations.keySet()){
            String lineName = routes_dict.get(key);
            Line line = new Line();
//            line.
        }
        for (int i = 0; i < routes_dict.keySet().size(); i++) {
            for (int j = 0; j < routeIdsToStations.keySet().size(); j++) {
                if ()
            }
        }
    }
}
