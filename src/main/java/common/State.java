package common;

import java.util.List;

public class State {
    private int tick;

    private List<UserState> userStates;

    public State(int tick) {
        this.tick = tick;
    }

    public State(int tick, List<UserState> userStates) {
        this.tick = tick;
        this.userStates = userStates;
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public List<UserState> getUserStates() {
        return userStates;
    }

    public void setUserStates(List<UserState> userStates) {
        this.userStates = userStates;
    }

    public void addUserState(UserState userState) {
        this.userStates.add(userState);
    }
}
