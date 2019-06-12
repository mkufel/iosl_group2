package dissemination;

import common.State;
import common.UserState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Random;

public class DisseminationEngine {
    private static final Random RANDOM;

    static {
        RANDOM = new Random();
    }

    public DisseminationEngine() {
        // TODO
    }

    public List<State> getStates(List<State> states) {
        // TODO

        // Find eligible agents (same train)
        // Create pairs

        return states;
    }

    public List<TrainState> findEligibleAgents(List<UserState> states) {

        List<UserState> statesAtEndOfLeg = states.stream()
                .filter(state -> state.getProgress() >= 1).collect(Collectors.toList());
        // Map of startStationId + endStationId as key
        Map<String, TrainState> trains = new HashMap<>();
        for (UserState state : statesAtEndOfLeg) {
            // Combine the edge ID with Line, as there might be trains travelling on the same tracks but different line
            String key = state.getEdgeId() + "_" + state.getLine();
            TrainState trainState = trains.get(key);
            if (trainState == null) {
                trainState = new TrainState(new ArrayList<>());
            }
            trainState.getPassengers().add(state);
            trains.put(key, trainState);
        }


        return new ArrayList<>(trains.values());
    }

    private List<UserStatePair> pairAgents(List<UserState> train) {
        return new ArrayList<>();
    }

    /**
     * Performs data exchange between two users.
     *
     * Exchange is performed with a given probability and may fail.
     * It will also fail, if the sender has no data to share or the receiver
     * already has this data.
     *
     * @param pair Pair of users, one of which is a data sender, another one is a receiver
     * @return True if the data exchange succeeded, false otherwise
     */
    private boolean exchange(UserStatePair pair) {
        if(!pair.getSender().isData() || pair.getReceiver().isData()) {
            return false;
        }

        if(!RANDOM.nextBoolean()) {
            return false;
        }

        pair.getReceiver().setData(true);

        return true;
    }

    private void persistDataTransfer(UserState receivedState) {

    }
}
