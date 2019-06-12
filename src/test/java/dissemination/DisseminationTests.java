package dissemination;

import common.UserState;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DisseminationTests {

    @Test
    public void testEligibleAgents() {
        List<UserState> states = new ArrayList<>();
        for (int personId = 1; personId < 10; personId++) {
            for (int tick = 0; tick < 50; tick++) {
                Long startStation = Long.valueOf(tick / 5);
                Long endStation = Long.valueOf((tick / 5) + 1);
                UserState state = new UserState(personId, startStation, endStation, "U1", (double) ((tick % 5) + 1) / 5, false);
                states.add(state);
            }
        }
        System.out.println(states);

        DisseminationEngine disseminationEngine = new DisseminationEngine();
        List<TrainState> trainStates = disseminationEngine.findEligibleAgents(states);
        System.out.println(trainStates);

    }
}
