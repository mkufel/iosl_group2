package TraceGenerationEngine;

import common.ScheduleItem;
import common.State;
import common.Station;
import common.UserState;
import init.InitEngine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

public class TraceGenerationEngine {
    InitEngine initEngine;
    ArrayList<Station> allUBahnStations;
    Map<String, ArrayList<ScheduleItem>> stopsWithSchedule;
    int total_users;
    int total_ticks;

    ConfigurationChangedListener configurationChangedListener;

    public static void main(String[] args) throws IOException {
//        InitEngine initEngine = new InitEngine();
        TraceGenerationEngine tce = new TraceGenerationEngine();
//        System.out.println(tce.getStates());
        System.out.println(tce.getStates());
//        Object a = initEngine.getStopsWithSchedule();
//        System.out.println(a);
    }

    public TraceGenerationEngine() {
        initEngine = new InitEngine();
        allUBahnStations = initEngine.getUBahnStations();
        stopsWithSchedule = initEngine.getStopsWithSchedule();
    }

    private String _getConfigValue(String key) throws IOException {
        String result = "";
        InputStream inputStream;

        try {
            Properties prop = new Properties();
            String propFileName = "config.properties";

            inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

            if (inputStream != null) {
                prop.load(inputStream);
            } else {
                throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
            }
            result = prop.getProperty(key);
            inputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public ArrayList<State> getStates() throws IOException {
        ArrayList<State> states = new ArrayList<State>();
        loadInitialPopulationValues();

        //initialise empty states
        for (int i = 0; i < total_ticks; i++) {
            states.add(new State(i, new ArrayList<>()));
        }
        Random rand = new Random();

        for (int i = 0; i < total_users; i++) {
            System.out.println("Generating state for user id: " + i);
            int startTick = rand.nextInt(total_ticks);
            Station startStation = getStartStation();
            Long endStation = null;
            int totalStops = 5 + rand.nextInt(11);
            State state;
            for (int tick = startTick; tick < startTick + totalStops; tick++) {
                if (tick >= total_ticks) {
                    break;
                }
                state = states.get(tick);
                if (this.stopsWithSchedule.get(Long.toString(startStation.getId())) != null) {
                    ScheduleItem someScheduleItem = (this.stopsWithSchedule.get(Long.toString(startStation.getId()))).get(0);
                    endStation = Long.parseLong(someScheduleItem.getNextStop_id());
                    UserState userState = new UserState(i, startStation.getId(), endStation, "U?", (double) (tick - startTick) / totalStops, false);
                    state.addUserState(userState);
                }
            }
        }
        return states;
    }

    private void loadInitialPopulationValues() throws IOException {
        total_users = Integer.parseInt(_getConfigValue("total_users"));
        total_ticks = Integer.parseInt(_getConfigValue("total_ticks"));

        if (this.configurationChangedListener != null) {
            this.configurationChangedListener.onConfigurationChanged(total_users, total_ticks);
        }

    }

    private Station getStartStation() throws IOException {
        ArrayList<Long> station_ids = new ArrayList<>();
        ArrayList<Double> popularities = new ArrayList<>();
        Double totalProbabilityCovered = 0.0;
        for (Station s : this.allUBahnStations) {
            Double station_popularity = Double.parseDouble(_getConfigValue("station." + s.getId()));
            if (station_popularity != null) {
                station_ids.add(s.getId());
                popularities.add(totalProbabilityCovered);
                totalProbabilityCovered += station_popularity;
            }
        }

        Random rand = new Random();

        Double randNum = rand.nextDouble() * totalProbabilityCovered;
        for (int i = 0; i < popularities.size() - 1; i++) {
            if (popularities.get(i) <= randNum && popularities.get(i + 1) > randNum) {
                int index = this.allUBahnStations.indexOf(new Station(station_ids.get(i)));
                if (index > -1) {
                    return this.allUBahnStations.get(index);
                } else {
                    return this.allUBahnStations.get(0);
                }
            }
        }
        return this.allUBahnStations.get(0);
    }

    public ConfigurationChangedListener getConfigurationChangedListener() {
        return configurationChangedListener;
    }

    public void setConfigurationChangedListener(ConfigurationChangedListener configurationChangedListener) {
        this.configurationChangedListener = configurationChangedListener;
    }
}
