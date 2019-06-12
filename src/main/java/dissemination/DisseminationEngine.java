package dissemination;

import common.State;
import common.UserState;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    private List<List<UserState>> findEligibleAgents(List<UserState> states) {
        return new ArrayList<>();
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

    /**
     * TODO
     *
     * @param receiverState
     */
    private void persistDataTransfer(UserState receiverState) {
        if(!receiverState.isData()) {
            return;
        }

        // TODO Write this.states
    }
}
