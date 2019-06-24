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

    /***
     * Attaches dissemination information to the states that have been passed in the contstructor.
     * For each state, it checks eligible agents, pairs the agents and exchanges information between them.
     * @return The modified state array.
     */
    public List<State> calculateDissemination() {

        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);
            if (state.getUserStates() != null && state.getUserStates().size() > 0) {
                state.getUserStates().get(0).setData(true);
                persistDataTransfer(state, state.getUserStates().get(0));
                break;
            }
        }

        for (State state : this.states) {
            for (TrainState train : findEligibleAgents(state.getUserStates())) {
                for (UserStatePair pair : pairAgents(train)) {
                    if (exchange(pair)) {
                        persistDataTransfer(state, pair.getReceiver());
                    }
                }
            }
        }

        return this.states;
    }

    /***
     * Given a list of UserStates, the method finds agents that are eligible for data transfer.
     * <p>
     *     Eligible means, that they are currently on the very end of their current leg and have not yet exchanged data.
     * </p>
     * @param states The list of UserStates to check
     * @return A list of TrainStates which represent users travelling together in the same train - and eligible for transfer.
     */
    public List<TrainState> findEligibleAgents(List<UserState> states) {

        List<UserState> statesAtEndOfLeg = states.stream()
                .filter(state -> state.getProgress() >= 0.8).collect(Collectors.toList());
        // statesAtEndOfLeg = states;
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

    /**
     * Given a train of users, this method finds pairs of users who have and those who do not have the data.
     *
     * @param train The TrainState of users.
     * @return A list of UserStatePairs.
     */
    public List<UserStatePair> pairAgents(TrainState train) {
        List<UserState> passengersWithData = train.getPassengers().stream()
                .filter(UserState::hasData).collect(Collectors.toList());
        List<UserState> passengersWithOutData = train.getPassengers().stream()
                .filter(state -> !state.hasData()).collect(Collectors.toList());

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
        if (!pair.getSender().hasData() || pair.getReceiver().hasData()) {
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
        if (!receiverState.hasData()) {
            return;
        }

        for (State state : this.states) {
            if (state.getTick() <= exchangeState.getTick()) {
                continue;
            }

            for (UserState user : state.getUserStates()) {
                if (user.getPersonId() == receiverState.getPersonId()) {
                    user.setData(true);
                    break;
                }
            }
            state.calculateActiveAgents();
        }
    }

    /**
     * Set states for the dissemination engine.
     *
     * @param states The states to work on.
     */
    public void setStates(List<State> states) {
        this.states = states;
    }
}
