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
     * If mapping routeIdsToLineNames doesn't exist, parses routes.csv and creates the mapping route Ids to Line names
     * If mapping routesToTrips doesn't exist, parses routes.csv and creates the mapping route Ids to Trip ids
     * Creates a mapping of stopIds to a list of their ScheduleItems (all trains leaving from the station)
     * @return Mapping stopId - ArrayList<ScheduleItem>
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
        if (routesToStations == null) routesToStations = mapRoutesToStations(routesToTrips, tripsToStations);

        // Add ubahn stations to the ArrayList of of Stations
        for (String key : routesToStations.keySet()) {
            ArrayList<Station> stations = routesToStations.get(key);
            allUBahnStations.addAll(stations);
        }

        // Filter the array, remove duplicates based on Station Id
        List<Station> distinctUbahnStations = allUBahnStations
                .stream()
                .filter(distinctByKey(Station::getId))
                .collect(Collectors.toList());

        return new ArrayList<>(distinctUbahnStations);
    }


    /**
     * Creates a common.Map - a set of Ubahn lines (id, name, List<Station>) based on the data from mappings
     * routes - trips, trips - stations, routeId - routeName
     * @return common.Map containing all Ubahn lines contained in the csv input files
     */
    public common.Map createMapFromBVGFiles() {
        if (routeIdsToLineNames == null) routeIdsToLineNames = this.readRoutesFromCSV("resources/routes.csv");
        if (routesToTrips == null) routesToTrips = this.mapRoutesToTripsFromCSV(routeIdsToLineNames, "resources/trips.csv");
        if (tripsToStations == null) tripsToStations = this.parseStationTimesFromCSV("resources/stop_times.csv");
        if (routesToStations == null) routesToStations = this.mapRoutesToStations(routesToTrips, tripsToStations);
        routesToStations = this.addStationCoordsToRouteStationsMapping(routesToStations, "resources/stops.csv");

        // update routesToStations to contain no duplicates
        routesToStations = removeDuplicatesFromRoutesToStations(routesToStations);

        return this.createMap(routesToStations, routeIdsToLineNames);
    }

    /**
     * Changes Ids of duplicate stations to corrected fixed Ids for given stations. Removes duplicate stations for each line.
     * @param routesToStationsWithDuplicates
     * @return routesToStationsWithoutDuplicates
     */
    private Map<String, ArrayList<Station>> removeDuplicatesFromRoutesToStations(Map<String, ArrayList<Station>> routesToStationsWithDuplicates) {
        if (stationsToDuplicateIds == null) stationsToDuplicateIds = this.getDuplicatesStopsFromCSV();
        Map<String, ArrayList<Station>> routesToStationsWithoutDuplicates = new HashMap<>();

        for (String route : routesToStationsWithDuplicates.keySet()) {
            ArrayList<Station> correctedStations = new ArrayList<>();
            for (Station checkedStation : routesToStationsWithDuplicates.get(route)) {

                // Find the fixed station supposed to replace the duplicates
                Station correctStation = stationsToDuplicateIds
                                .keySet()
                                .stream()
                                .filter( x -> stationsToDuplicateIds.get(x).contains(checkedStation.getId()))
                                .findFirst()
                                .orElse(null);

                // Correct the station Id and add it to the array of corrected Stations
                checkedStation.setId(correctStation.getId());
                correctedStations.add(checkedStation);
            }

            // remove any duplicates stations based on Id in the lines - to Prevent routes like A -> A -> B
            correctedStations = (ArrayList<Station>) correctedStations
                    .stream()
                    .filter(distinctByKey(Station::getId))
                    .collect(Collectors.toList());

            routesToStationsWithoutDuplicates.put(route, correctedStations);
        }

        return routesToStationsWithoutDuplicates;
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    /**
     * Parses the routes.csv file and creates a mapping of routeId - routeName
     *
     * @param fileName input file "routes.csv"
     * @return Mapping routeId -> UbahnLineName
     */
    private Map<String, String> readRoutesFromCSV(String fileName) {
        Map<String, String> routeIdToLineName_dict = new HashMap<>();
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
                    routeIdToLineName_dict.put(nextRecord[0], nextRecord[2]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routeIdToLineName_dict;
    }


    /**
     * Parses the trips.csv and creates a mapping of routeId - List<tripId>
     *
     * @param routeIdToLineName Dictionary routeId -> UbahnLineName
     * @param fileName    path to trips.csv
     * @return Hashmap, key:routeId, value: ArrayList<tripId>
     */
    private Map<String, ArrayList<String>> mapRoutesToTripsFromCSV(Map<String, String> routeIdToLineName, String fileName) {
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
                if (routeIdToLineName.containsKey(route_id)) {

                    // If the route_id - List<Trips> mapping already exists, append the current trip_id. Else create a new entry
                    if (routesToTrips_dict.containsKey(route_id)) {
                        currentTrips = routesToTrips_dict.get(route_id);
                        currentTrips.add(trip_id);
                        routesToTrips_dict.put(route_id, currentTrips);
                    } else {
                        routesToTrips_dict.put(route_id, new ArrayList<>(Arrays.asList(trip_id)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routesToTrips_dict;
    }


    /**
     * Parse stop_times.csv and creates a mapping tripId - ArrayList<stationId>
     *
     * @param fileName path to stop_times.csv
     * @return mapping tripId - ArrayList<stationId>
     */
    private Map<String, ArrayList<Station>> parseStationTimesFromCSV(String fileName) {
        Path pathToFile = Paths.get(fileName);
        Map<String, ArrayList<Station>> tripIdsToStations_dict = new HashMap<>();

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

                    // If the trip_id - List<Station> already exists, append the current stationId. Else create a new entry
                    if (tripIdsToStations_dict.containsKey(trip_id)) {
                        currentStations = tripIdsToStations_dict.get(trip_id);
                        currentStations.add(new Station(station_id));
                        tripIdsToStations_dict.put(trip_id, currentStations);
                    } else {
                        tripIdsToStations_dict.put(trip_id, new ArrayList<>(Collections.singletonList(new Station(station_id))));
                    }
                } catch (NumberFormatException ex) {
                    System.out.println("Skipped station with id: " + nextRecord[3]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return tripIdsToStations_dict;
    }


    /**
     * Creates a mapping routeId - List<stationId> based on the mappings routeId - List<tripId> and tripId - List<stationId>
     *
     * @param routesToTrips_dict   mapping routeId - List<tripId>
     * @param tripsToStations_dict mapping tripId - List<stationId>
     * @return mapping routeId - List<stationId>
     */
    private Map<String, ArrayList<Station>> mapRoutesToStations(Map<String, ArrayList<String>> routesToTrips_dict, Map<String, ArrayList<Station>> tripsToStations_dict) {
        Map<String, ArrayList<Station>> routesToStations = new HashMap<>();

        for (String routeId : routesToTrips_dict.keySet()) {
            int max = 0;
            String maxTripId = "";
            // Loop through all trips for the current route and select one with the largest number of stations.
            // Trips do no regularly go through all stations on a route. It is crucial to get all stations of the path
            // for the visualization of full Ubahn lines in the map.
            for (String tripId : routesToTrips_dict.get(routeId)) {
                int temp = tripsToStations_dict.get(tripId).size();
                if (temp > max) {
                    max = temp;
                    maxTripId = tripId;
                }
            }

            routesToStations.put(routeId, tripsToStations_dict.get(maxTripId));
        }

        return routesToStations;
    }


    /**
     * Parses stops.csv and extends the mapping routeId - List<Station> with GeoCoordinates of each station.
     *
     * @param routesToStations mapping of routeId - List<stationId>
     * @param fileName path to stops.csv
     * @return mapping of routeId - List<Station>
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

                    // Loop through stations in every route
                    for (String key : routesToStations.keySet()) {
                        for (Station station : routesToStations.get(key)) {
                            // If id of the station matches an id of a station from the csv file, update the coords
                            if (station.getId().equals(Long.parseLong(nextRecord[0]))) {
                                station.setName(name);
                                station.setLocation(new GeoCoords(stop_lat, stop_lon));
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Skipped station with id: " + nextRecord[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return routesToStations;
    }


    /**
     * Creates a List of lines based on the mapping routesIdsToStations, extracts names of U-bahn lines based on
     * their identifier and creates a Line for each U-bahn line. Then adds all lines to a Map and returns it.
     * @param routeIdsToStations Mapping of routeIds to StationIds
     * @param routeIdsToLineNames Mapping of routeIds to route names
     * @return Map object containing all U-bahn lines.
     */
    private common.Map createMap(Map<String, ArrayList<Station>> routeIdsToStations, Map<String, String> routeIdsToLineNames) {
        common.Map map = new common.Map();
        ArrayList<Line> lines = new ArrayList<>();

        // For each route from the mapping add 2 lines. The first line with stations in the original order and the
        // second one with reversed stations to allow bidirectional movement along each Ubahn route in the Map
        for (String routeId : routeIdsToStations.keySet()) {
            String lineName = routeIdsToLineNames.get(routeId);

            Line line = new Line();
            line.setName(lineName);
            ArrayList<Station> stations = routeIdsToStations.get(routeId);
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


    /**
     * Parses stop_times.csv and for each of the stops creates a List<ScheduleItem> representing trains leaving from
     * the station throughout the day.
     * @param fileName path to stop_times.csv
     * @param routeIdsToTripIds
     * @param routesIdsToLineNames
     * @return Map<Long: stopId, ArrayList<ScheduleItem>> for each of the stations from stop_times.csv
     */
    private Map<Long, ArrayList<ScheduleItem>> mapStopsToScheduleItems(String fileName, Map<String, ArrayList<String>> routeIdsToTripIds, Map<String, String> routesIdsToLineNames) {
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

                // Read the second entry
                if ((nextNextRecord = csvReader.readNext()) != null) {

                    // If extracted line_name based on the trip_id, then it's an ubahn line, update stopToScheduleItems mapping
                    if (!(line_name = getLineNameFromTripId(nextRecord[0], routeIdsToTripIds, routesIdsToLineNames)).equals("")) {
                        stopsToScheduleItems = updateStopsToScheduleItemsMap(stopsToScheduleItems, nextRecord, nextNextRecord, line_name);
                    }

                    nextRecord = nextNextRecord;
                }
            }

            // Read the nextNextRecord consecutive to nextRecord
            while ((nextNextRecord = csvReader.readNext()) != null) {

                // If extracted line_name based on the trip_id, then it's an ubahn line, update stopToScheduleItems mapping
                if (!(line_name = getLineNameFromTripId(nextRecord[0], routeIdsToTripIds, routesIdsToLineNames)).equals("")) {
                    stopsToScheduleItems = updateStopsToScheduleItemsMap(stopsToScheduleItems, nextRecord, nextNextRecord, line_name);
                }

                nextRecord = nextNextRecord;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        stopsToScheduleItems = this.removeInvalidScheduleItems(stopsToScheduleItems);

        return stopsToScheduleItems;
    }


    /**
     * Checks if nextRecord and nextNextRecord are on the same trip and if so then it creates a new schedule item
     * for a stop from nextRecord to a stop from nextNextRecord. If any of the stopIds is a duplicate, it is replaced
     * with an id of a fixed Station corresponding to the duplicate
     * @param stopsToScheduleItems
     * @param nextRecord parsed from stops_times.csv
     * @param nextNextRecord parsed from stops_times.csv, proceeding nextRecord
     * @param lineName name of the Ubahn serving between nextRecord and nextNextRecord
     * @return stopsToScheduleItems with a new ScheduleItem for station in nextRecord directed to the station in nextNextRecord
     */
    private Map<Long, ArrayList<ScheduleItem>> updateStopsToScheduleItemsMap(Map<Long, ArrayList<ScheduleItem>> stopsToScheduleItems, String[] nextRecord, String[] nextNextRecord, String lineName) {

        // If both entries belong to the same trip [0] and are consecutive in the stop sequence [4],
        // extract the line name and if U-bahn create an entry in stopsToScheduleItem
        if (nextRecord[0].equals(nextNextRecord[0]) && Long.parseLong(nextRecord[4]) + 1 == Long.parseLong(nextNextRecord[4])) {

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
                tempScheduleItems.add(new ScheduleItem(lineName, departure_time, nextStop_id));
                stopsToScheduleItems.put(stop_id, tempScheduleItems);
            }
            // Else create a new entry Key: stopId, Value: ArrayList<ScheduleItem>
            else {
                tempScheduleItems = new ArrayList<>();
                tempScheduleItems.add(new ScheduleItem(lineName, departure_time, nextStop_id));
                stopsToScheduleItems.put(stop_id, tempScheduleItems);
            }
        }

        return stopsToScheduleItems;
    }


    /**
     * Returns a name of an U-bahn line that serves a given trip
     * @param tripId
     * @param routeIdsToTrips Mapping of routeIds to TripIds
     * @param routesIdsToLineNames Mapping of routeIds to line names
     * @return String U-bahn name corresponding to trip_id
     */
    private String getLineNameFromTripId(String tripId, Map<String, ArrayList<String>> routeIdsToTrips, Map<String, String> routesIdsToLineNames) {
        String line_name = "";

        // Check if a given tripId is included in a list of trips for a given route.
        // If so extract the line name corresponding to the route_id and set the variable
        for (String key : routeIdsToTrips.keySet()) {
            if (routeIdsToTrips.get(key).contains(tripId)) {
                line_name = routesIdsToLineNames.get(key);
                return line_name;
            }
        }

        return line_name;
    }


    /**
     * Parses stops.csv and creates a mapping of Station - List<duplicateStationId> for stations with the same geo location.
     * Stations with the same geo-location can be represented by multiple station instances in the stops.csv.
     * For the mapping, it selects a single instance and maps it to a list of ids of all duplicate stations.
     * @return mapping Station - List<Long: duplicateStationId>
     */
    private Map<Station, ArrayList<Long>> getDuplicatesStopsFromCSV() {
        Map<Station, ArrayList<Long>> mapStationsToDuplicateIds = new HashMap<>();
        Path pathToStops = Paths.get("resources/stops.csv");

        try (
                Reader reader = Files.newBufferedReader(pathToStops);
                CSVReader csvReader = new CSVReader(reader, ',', '"', 1);
        ) {
            String[] nextRecord;

            while ((nextRecord = csvReader.readNext()) != null) {
                Double lat = Double.parseDouble(nextRecord[4]);
                Double lon = Double.parseDouble(nextRecord[5]);
                Long id = Long.parseLong(nextRecord[0]);
                String name = nextRecord[2];

                Station correctStation = mapStationsToDuplicateIds.keySet()
                        .stream()
                        .filter(x -> lat.equals(x.getLocation().getLat()) && lon.equals(x.getLocation().getLon()))
                        .findFirst()
                        .orElse(null);

                if (correctStation != null) {
                    ArrayList<Long> stationAliases = mapStationsToDuplicateIds.get(correctStation);
                    stationAliases.add(id);
                    mapStationsToDuplicateIds.put(correctStation, stationAliases);
                } else {
                    Station station = new Station(id, name, new GeoCoords(lat, lon));
                    ArrayList<Long> stationAliases = new ArrayList<>(Collections.singletonList(id));
                    mapStationsToDuplicateIds.put(station, stationAliases);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return mapStationsToDuplicateIds;
    }


    /**
     * Removes Schedule items that lead to non-adjacent stations (not directly preceeding and directly suceeding) a given station.
     * Such items appear sporadically in the BVG schedule, we remove them to prevent errors in the Map/Graph which has
     * only edges between adjacent stations.
     * @param stopsToScheduleItems
     * @return stopsToScheduleItemsWithoutInvalidItems
     */
    private Map<Long, ArrayList<ScheduleItem>> removeInvalidScheduleItems(Map<Long, ArrayList<ScheduleItem>> stopsToScheduleItems) {

        // Create a copy of the map to prevent the problem of concurrent modification while looping through
        Map<Long, ArrayList<ScheduleItem>> stopsToScheduleItemsWithoutInvalidItems = new HashMap<>();
        stopsToScheduleItems
                .keySet()
                .forEach(x -> stopsToScheduleItemsWithoutInvalidItems.put(x, new ArrayList<>()));

        // Loop through the ScheduleItems
        for (Long stopId : stopsToScheduleItems.keySet()) {
            ArrayList<Long> possibleNextStops = getListOfPossibleNextStops(stopId);
            for (ScheduleItem item : stopsToScheduleItems.get(stopId)) {

                // If found a schedule item that leads to an adjacent station, add it to the copy of the Map
                if (possibleNextStops.contains(item.getNextStop_id())) {
                    ArrayList<ScheduleItem> tempItems = stopsToScheduleItemsWithoutInvalidItems.get(stopId);
                    tempItems.add(item);
                    stopsToScheduleItemsWithoutInvalidItems.put(stopId, tempItems);
                }
            }
        }

        return stopsToScheduleItemsWithoutInvalidItems;
    }

    /**
     * Returns a List<stopId> of stops adjacent (directly preceeding and suceeding) to stopId.
     * Note that a stop can be a part of more than one Ubahn line.
     * @param stopId for which adjacent stops are looked for
     * @return List<Long: stopId> adjacent to stopId
     */
    private ArrayList<Long> getListOfPossibleNextStops(Long stopId) {
        ArrayList<Long> stops = new ArrayList<>();

        for (String route : routesToStations.keySet()) {
            ArrayList<Station> routeStations = routesToStations.get(route);

            for (int i = 0; i < routeStations.size(); i++) {
                if (routeStations.get(i).getId().equals(stopId)) {
                    try {
                        stops.add(routeStations.get(i - 1).getId());
                    } catch (Exception e) {}
                    try{
                        stops.add(routeStations.get(i + 1).getId());
                    } catch (Exception e) {}
                }
            }
        }

        return stops;
    }
}
