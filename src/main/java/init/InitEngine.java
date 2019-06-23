package init;

import com.opencsv.CSVReader;
import common.GeoCoords;
import common.Line;
import common.ScheduleItem;
import common.Station;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class InitEngine {

    Map<String, String> routeIdsToLineNames;
    Map<String, ArrayList<String>> routesToTrips;
    Map<Long, ArrayList<ScheduleItem>> stopsToScheduleItems;
    Map<String, ArrayList<Station>> routesToStations;
    Map<String, ArrayList<Station>> tripsToStations;
    Map<Station, ArrayList<Long>> stationsToDuplicateIds;

    public InitEngine() {
    }

    /**
     * If mapping routeIdsToLineNames doesn't exist, parses routes.csv and creates the mapping
     * If mapping routesToTrips doesn't exist, parses routes.csv and creates the mapping
     * Creates a mapping of stopIds to a list of their ScheduleItems (all trains leaving from the station)
     * @return Mapping stopId - ScheduleItem[]
     */
    public Map<Long, ArrayList<ScheduleItem>> getStopsWithSchedule() {
        System.out.println("Parsing stops with their schedules...");

        if (routeIdsToLineNames == null) routeIdsToLineNames = this.readRoutesFromCSV("resources/routes.csv");
        if (routesToTrips == null) routesToTrips = this.mapRoutesToTripsFromCSV(routeIdsToLineNames, "resources/trips.csv");
        if (stopsToScheduleItems == null) stopsToScheduleItems = this.mapStopsToScheduleItems("resources/stop_times.csv", routesToTrips, routeIdsToLineNames);

        return stopsToScheduleItems;
    }

    /**
     * From mappings routes - trips and trips - stations, create a mapping routes - stations.
     * Then use the mapping of routeId - routeName to extract line names.
     * Finally, create a list of all Stations (line names used as parameters).
     * @return List of Stations
     */
    public ArrayList<Station> getUBahnStations() {
        System.out.println("Parsing U-Bahn stations...");
        ArrayList<Station> allUBahnStations = new ArrayList();

        if (routeIdsToLineNames == null) routeIdsToLineNames = readRoutesFromCSV("resources/routes.csv");
        if (routesToTrips == null) routesToTrips = mapRoutesToTripsFromCSV(routeIdsToLineNames, "resources/trips.csv");
        if (tripsToStations == null) tripsToStations = parseStationTimesFromCSV("resources/stop_times.csv");
        if (routesToStations == null) routesToStations = mapRouteToStations(routesToTrips, tripsToStations);

        for (String key : routesToStations.keySet()) {
            ArrayList<Station> stations = routesToStations.get(key);
            allUBahnStations.addAll(stations);
        }

        List<Station> distinctUbahnStations = allUBahnStations
                .stream()
                .filter(distinctByKey(Station::getId))
                .collect(Collectors.toList());

        return new ArrayList<Station>(distinctUbahnStations);
    }


    public common.Map createMapFromBVGFiles() {
        if (routeIdsToLineNames == null) routeIdsToLineNames = this.readRoutesFromCSV("resources/routes.csv");
        if (routesToTrips == null) routesToTrips = this.mapRoutesToTripsFromCSV(routeIdsToLineNames, "resources/trips.csv");
        if (tripsToStations == null) tripsToStations = this.parseStationTimesFromCSV("resources/stop_times.csv");
        if (routesToStations == null) routesToStations = this.mapRouteToStations(routesToTrips, tripsToStations);
        routesToStations = this.addStationCoordsToRouteStationsMapping(routesToStations, "resources/stops.csv");

        //remove duplicate station ids
        if (stationsToDuplicateIds == null) stationsToDuplicateIds = this.getDuplicatesStopsFromCSV();
        Map<String, ArrayList<Station>> routesToStationsWithoutDuplicates = new HashMap<>();

        for (String route : routesToStations.keySet()) {
            ArrayList<Station> correctedStations = new ArrayList<>();

            for (Station checkedStation : routesToStations.get(route)) {
                // if the station id doesn't match any of the keys in the mapping stationsToDuplicateIds, then it's a duplicate
                if (stationsToDuplicateIds.keySet().stream().noneMatch(x -> x.getId().equals(checkedStation.getId()))) {
                    for (Station correctStation : stationsToDuplicateIds.keySet()) {

                        if (stationsToDuplicateIds.get(correctStation).contains(checkedStation.getId())) {
                            correctedStations.add(new Station(
                                    correctStation.getId(),
                                    checkedStation.getName(),
                                    checkedStation.getLocation(),
                                    checkedStation.getPopulation())
                            );
                        }
                    }
                } else {
                    correctedStations.add(checkedStation);
                }
            }

            correctedStations = (ArrayList<Station>) correctedStations
                    .stream()
                    .filter(distinctByKey(Station::getId))
                    .collect(Collectors.toList());

            routesToStationsWithoutDuplicates.put(route, correctedStations);
        }

        routesToStations = routesToStationsWithoutDuplicates;

        return this.createMap(routesToStations, routeIdsToLineNames);
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Parse the CSV file mapping ubahn lines to route identifiers
     *
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routes_dict;
    }


    /**
     * Create a mapping of route_id to trips at a given route from trips.csv
     *
     * @param routes_dict Dictionary route_id -> ubahn_line_name
     * @param fileName    path to trips.csv
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routesToTrips_dict;
    }


    /**
     * Parse stop_times.csv to create a mapping trip_id - ArrayList<station_id>
     *
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
                try {
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
                } catch (NumberFormatException ex) {
                    System.out.println("Skipped station with id: " + nextRecord[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tripsToStations_dict;
    }


    /**
     * Create a mapping of route_id - List<station_id> </station_id>based on the mappings of route_id - List<trip_id>
     * and trip_id - List<station_id>
     *
     * @param routesToTrips_dict   mapping route_id - List<trip_id>
     * @param tripsToStations_dict mapping trip_id - List<station_id>
     * @return mapping route_id - List<station_id>
     */
    private Map<String, ArrayList<Station>> mapRouteToStations(Map<String, ArrayList<String>> routesToTrips_dict, Map<String, ArrayList<Station>> tripsToStations_dict) {
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
     *
     * @param routesToStations mapping of route_id - List<station_id>
     * @param fileName         path to stops.csv
     * @return mapping of route_id - List<Station>
     */
    private Map<String, ArrayList<Station>> addStationCoordsToRouteStationsMapping(Map<String, ArrayList<Station>> routesToStations, String fileName) {
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
        ) {
            String[] nextRecord;
            while ((nextRecord = csvReader.readNext()) != null) {
                try {
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
                } catch (NumberFormatException ex) {
                    System.out.println("Skipped station with id: " + nextRecord[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routesToStations;
    }


    /**
     * Creates a List of lines based on the mapping routesIdsToStations, extracts names of u-bahn lines based on
     * their identifier and instantiates Line objects for each U-bahn line. Then adds all lines to a Map and returns it.
     * @param routeIdsToStations Mapping of routeIds to StationIds
     * @param routes_dict Mapping of routeIds to route names
     * @return Map object containing all U-bahn lines.
     */
    private common.Map createMap(Map<String, ArrayList<Station>> routeIdsToStations, Map<String, String> routes_dict) {
        common.Map map = new common.Map();
        ArrayList<Line> lines = new ArrayList<>();
        for (String key : routeIdsToStations.keySet()) {
            String lineName = routes_dict.get(key);
            Line line = new Line();
            line.setName(lineName);

            ArrayList<Station> stations = routeIdsToStations.get(key);
            line.setStations(stations);
            lines.add(line);

            Line lineReversed = new Line();
            lineReversed.setName(lineName);
            ArrayList<Station> reversedStations = new ArrayList<>();
            for (Station station : stations) {
                reversedStations.add(0, new Station(station.getId(), station.getName(), station.getLocation(), station.getPopulation()));
            }

            lineReversed.setStations(reversedStations);
            lines.add(lineReversed);

        }

        map.setLines(lines);
        return map;
    }


    private Map<Long, ArrayList<ScheduleItem>> mapStopsToScheduleItems(String fileName, Map<String, ArrayList<String>> routeIdsToTrips, Map<String, String> routesIdsToLineNames) {
        Map<Long, ArrayList<ScheduleItem>> stopsToScheduleItems = new HashMap<>();
        Path pathToFile = Paths.get(fileName);

        try (
                Reader reader = Files.newBufferedReader(pathToFile);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
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
        } catch (Exception e) {
            e.printStackTrace();
        }

        return stopsToScheduleItems;
    }


    private Map<Long, ArrayList<ScheduleItem>> updateStopsToScheduleItemsMap(Map<Long, ArrayList<ScheduleItem>> stopsToScheduleItems, String[] nextRecord, String[] nextNextRecord, String line_name) {

        // If both entries belong to the same trip [0] and are consecutive in the stop sequence [4],
        // extract the line name and if U-bahn create an entry in stopsToScheduleItem
        if (nextRecord[0].equals(nextNextRecord[0]) && Integer.parseInt(nextRecord[4]) + 1 == Integer.parseInt(nextNextRecord[4])) {

            ArrayList<ScheduleItem> tempScheduleItems;
            String departure_time = nextRecord[2];
            Long stop_id = Long.parseLong(nextRecord[3]);
            Long nextStop_id = Long.parseLong(nextNextRecord[3]);

            for (Station station : stationsToDuplicateIds.keySet()) {
                if (stationsToDuplicateIds.get(station).contains(stop_id)) {
                    stop_id = station.getId();
                }
            }

            for (Station station : stationsToDuplicateIds.keySet()) {
                if (stationsToDuplicateIds.get(station).contains(nextStop_id)) {
                    nextStop_id = station.getId();
                }
            }

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


    /**
     * Returns a name of an U-bahn line that serves a given trip
     * @param trip_id
     * @param routeIdsToTrips Mapping of routeIds to TripIds
     * @param routesIdsToLineNames Mapping of routeIds to line names
     * @return String U-bahn name corresponding to trip_id
     */
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


    /**
     *
     * @return
     */
    private Map<Station, ArrayList<Long>> getDuplicatesStopsFromCSV() {
        Map<Station, ArrayList<Long>> mapStationsToDuplicateIds = new HashMap<>();
        Path pathtoStops = Paths.get("resources/stops.csv");

        try (
                Reader reader = Files.newBufferedReader(pathtoStops);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
        ) {
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                Double lat = Double.parseDouble(nextRecord[4]);
                Double lon = Double.parseDouble(nextRecord[5]);
                Long id = Long.parseLong(nextRecord[0]);
                String name = nextRecord[2];

                Optional<Station> optStation = mapStationsToDuplicateIds.keySet()
                        .stream()
                        .filter(x -> lat.equals(x.getLocation().getLat()) && lon.equals(x.getLocation().getLon()))
                        .findFirst();

                if (optStation.isPresent()) {
                    ArrayList<Long> stationAliases = mapStationsToDuplicateIds.get(optStation.get());
                    stationAliases.add(id);
                    mapStationsToDuplicateIds.put(optStation.get(), stationAliases);
                } else {
                    Station station = new Station(id, name, new GeoCoords(lat, lon));
                    ArrayList<Long> stationAliases = new ArrayList<>(Arrays.asList(id));
                    mapStationsToDuplicateIds.put(station, stationAliases);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapStationsToDuplicateIds;
    }

}
