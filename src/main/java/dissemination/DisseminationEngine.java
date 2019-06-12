package dissemination;

import common.State;
import common.UserState;

import java.util.*;
import java.util.stream.Collectors;

public class DisseminationEngine {
    private static final Random RANDOM;

    private List<State> states;

    static {
        RANDOM = new Random();
    }

    public DisseminationEngine(List<State> states) {
        this.states = states;
    }

    public List<State> getStates() {
        // TODO

        return this.states;
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

    public List<UserStatePair> pairAgents(TrainState train) {
        List<UserState> passengersWithData = train.getPassengers().stream()
                .filter(UserState::isData).collect(Collectors.toList());
        List<UserState> passengersWithOutData = train.getPassengers().stream()
                .filter(state -> !state.isData()).collect(Collectors.toList());

        List<UserStatePair> pairs = new ArrayList<>();
        for (UserState stateWithData : passengersWithData) {
            if (passengersWithOutData.size() > 0) {
                UserState stateWithoutData = passengersWithOutData.remove(0);
                UserStatePair pairedStates = new UserStatePair(stateWithData, stateWithoutData);
                pairs.add(pairedStates);
            }
        }

        return pairs;
    }

    /**
     * Performs data exchange between two users.
     * <p>
     * Exchange is performed with a given probability and may fail.
     * It will also fail, if the sender has no data to share or the receiver
     * already has this data.
     *
     * @param pair Pair of users, one of which is a data sender, another one is a receiver
     * @return True if the data exchange succeeded, false otherwise
     */
    private boolean exchange(UserStatePair pair) {
        if (!pair.getSender().isData() || pair.getReceiver().isData()) {
            return false;
        }

        if (!RANDOM.nextBoolean()) {
            return false;
        }

        pair.getReceiver().setData(true);

        return true;
    }

    /**
     * Marks given user as a data carrier in every state where the user appears.
     *
     * @param receiverState Data receiver
     * @param exchangeState State when the data exchange happens
     */
    private void persistDataTransfer(State exchangeState, UserState receiverState) {
        if(!receiverState.isData()) {
            return;
        }

        for(State state : this.states) {
            if(state.getTick() <= exchangeState.getTick()) {
                continue;
            }

            for(UserState user : state.getUserStates()) {
                if(user.getPersonId() == receiverState.getPersonId()) {
                    user.setData(true);
                    break;
                }
            }
        }
    }
}
