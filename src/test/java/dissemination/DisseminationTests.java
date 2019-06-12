package dissemination;

import common.State;
import common.UserState;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DisseminationTests {

    @Test
    public void testEligibleAgents() {
        List<State> states = getStates();
        List<UserState> userStates = states.get(5).getUserStates();

        System.out.println(userStates);

        DisseminationEngine disseminationEngine = new DisseminationEngine(states);
        List<TrainState> trainStates = disseminationEngine.findEligibleAgents(userStates);
        System.out.println(trainStates);
    }

    @Test
    public void testPairing() {
        List<State> states = getStates();
        List<UserState> userStates = states.get(4).getUserStates();

        DisseminationEngine disseminationEngine = new DisseminationEngine(states);

        List<TrainState> trainStates = disseminationEngine.findEligibleAgents(userStates);
        List<UserStatePair> pairs = disseminationEngine.pairAgents(trainStates.get(0));

        System.out.println(pairs);

        Assert.assertTrue(pairs.size() == 1);
    }

    private List<State> getStates() {
        List<State> states = new ArrayList<>();

        for(int tick = 0; tick < 50; tick++) {
            State state = new State(tick, new ArrayList<>());

            for (int personId = 1; personId < 10; personId++) {
                Long startStation = (long) (tick / 5);
                Long endStation = (long) ((tick / 5) + 1);

                UserState userState = new UserState(personId, startStation, endStation, "U1", (double) ((tick % 5) + 1) / 5, personId == 1);

                state.addUserState(userState);
            }

            states.add(state);
        }

        return states;
    }
}
