package visualization.services;

import common.State;
import common.UserState;

import java.util.ArrayList;
import java.util.List;

public class StateFactory {
    public static List<State> createStates() {
        List<State> states = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            List<UserState> users = new ArrayList<>();

<<<<<<< Updated upstream
            users.add(new UserState(42, 1, 2, "Line", i / 3.0, false));
=======
            users.add(new UserState(42, "1", "2", "Line", i / 3.0, false));
>>>>>>> Stashed changes

            states.add(new State(i, users));
        }

        return states;
    }
}
