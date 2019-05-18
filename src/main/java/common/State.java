package common;

import java.util.List;

public class State {
    private int tick;

    private List<UserState> users;

    public State(int tick, List<UserState> users) {
        this.tick = tick;
        this.users = users;
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
}
