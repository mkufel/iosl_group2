package common;

import java.util.List;

public class State {
    private int tick;

    private List<UserState> userStates;

    private int activeAgents = 0;

    public State(int tick, List<UserState> users) {
        this.tick = tick;
        this.userStates = users;
        this.activeAgents = this.userStates.stream()
                .reduce(0, (totalActive, user) -> totalActive + (user.isData() ? 1 : 0), Integer::sum);
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

    public int getActiveAgents() {
        return activeAgents;
    }

    public void setActiveAgents(int activeAgents) {
        this.activeAgents = activeAgents;
    }
}
