package TraceGenerationEngine;

import common.ScheduleItem;
import common.State;
import common.Station;
import common.UserState;
import init.InitEngine;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class TraceGenerationEngine {
    InitEngine initEngine;
    ArrayList<Station> allUBahnStations;
    Map<String, ArrayList<ScheduleItem>> stopsWithSchedule;

    int total_users;
    int total_ticks;

    ConfigurationChangedListener configurationChangedListener;

    public static void main(String[] args) throws IOException {
        TraceGenerationEngine tce = new TraceGenerationEngine();
        System.out.println(tce.getStates());
    }

    public TraceGenerationEngine() {
        initEngine = new InitEngine();
        allUBahnStations = initEngine.getUBahnStations();
        stopsWithSchedule = initEngine.getStopsWithSchedule();
    }

    public int getTotal_users() {
        return total_users;
    }

    public int getTotal_ticks() {
        return total_ticks;
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

    public List<State> getStates() throws IOException {
        ArrayList<State> states = new ArrayList<>();
        loadInitialPopulationValues();

        //initialise empty states
        for (int i = 0; i < total_ticks; i++) {
            states.add(new State(i, new ArrayList<>()));
        }
        Random rand = new Random();

        for (int i = 0; i < total_users; i++) {
            System.out.println("Generating state for user id: " + i);
            int startTick = rand.nextInt(total_ticks);

            Long startStation = getStartStation().getId();
            Long endStation = startStation;

            int TOTAL_STOPS = 5 + rand.nextInt(11);
            State state;
            Double progress = 0.0;
            String lineName = "";
            for (int tick = startTick; (tick < startTick + TOTAL_STOPS) && (tick < total_ticks); tick++) {
                state = states.get(tick);

                //User is inside a train
                if (progress > 0.0) {
                    UserState userState = new UserState(i, startStation, endStation, lineName, progress, false);
                    state.addUserState(userState);
                    progress += 0.2; //TODO: calculate progress based on length of leg
                } else { //User is at a station
                    ArrayList<ScheduleItem> scheduleFromStartStation = this.stopsWithSchedule.get(Long.toString(startStation));
                    if(scheduleFromStartStation != null) {
                        ScheduleItem nextScheduleItem = getNextScheduleItem(scheduleFromStartStation, tick, startStation);
                        //Found a train in the next 5 minutes
                        if (nextScheduleItem != null && nextScheduleItem.getNextStop_id() != null) {
                            endStation = Long.parseLong(nextScheduleItem.getNextStop_id());
                            lineName = nextScheduleItem.getLine_name();
                        }
                        UserState userState = new UserState(i, startStation, endStation, lineName, progress, false);
                        state.addUserState(userState);
                        progress += 0.2; //TODO: calculate progress based on length of leg
                    }
                }

                // User has reached a station
                if (progress >= 1.0) {
                    progress = 0.0;
                    startStation = endStation;
                }
            }
        }
        return states;
    }

    private ScheduleItem getNextScheduleItem(ArrayList<ScheduleItem> scheduleItems, int tick, Long currentStation) throws IOException {
        String startTime = this._getConfigValue("start_time");
        int currentTime = getAbsoluteTimeFromTick(startTime, tick);
        ArrayList<ScheduleItem> scheduleItemsToBeConsidered= new ArrayList<>();
        for (ScheduleItem scheduleItem: scheduleItems) {
            String dt = scheduleItem.getDeparture_time();
            String[] components = dt.split(":");
            int departureTimeInSeconds = Integer.parseInt(components[0])*60*60
                                            + Integer.parseInt(components[1])*60
                                            + Integer.parseInt(components[2]);
            if (departureTimeInSeconds > currentTime && departureTimeInSeconds - currentTime < 10*60) {
                scheduleItemsToBeConsidered.add(scheduleItem);
            }
        }
        if (scheduleItemsToBeConsidered.size() == 0) {
            return null;
        }
        Random rand = new Random();

        int randNum = rand.nextInt(scheduleItemsToBeConsidered.size());


        return scheduleItemsToBeConsidered.get(randNum);
    }

    private int getAbsoluteTimeFromTick(String startTime, int tick) throws IOException {
        //Each tick is 30 seconds
        int timeElapsedSinceStart = tick * 30;
        return Integer.parseInt(startTime) * 60 * 60 + timeElapsedSinceStart;
    }
//        int hours = currentTime/(60*60);
//        int minutes = (currentTime - (hours * 60 * 60))/60;
//        int seconds = (currentTime - (minutes * 60));
//        return String.format("%d:%d:%d", hours, minutes, seconds);
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
