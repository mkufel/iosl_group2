package common;

import java.util.List;

public class State {
    private int tick;

    private List<UserState> users;

    private int activeAgents = 0;

    public State(int tick, List<UserState> users) {
        this.tick = tick;
        this.users = users;
        this.activeAgents = this.users.stream()
                .reduce(0, (totalActive, user) -> totalActive + (user.isData() ? 1 : 0), Integer::sum);
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    public List<UserState> getUsers() {
        return users;
    }

    public void setUsers(List<UserState> users) {
        this.users = users;
    }

    public int getActiveAgents() {
        return activeAgents;
    }

    public void setActiveAgents(int activeAgents) {
        this.activeAgents = activeAgents;
    }
}
