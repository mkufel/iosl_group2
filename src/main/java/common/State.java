package common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Simulation state identified by the tick.
 *
 * Contains states of all agents that are present in the simulation at the given tick.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class State {

    @JsonProperty("tick")
    private int tick;

    @JsonProperty("userStates")
    private List<UserState> userStates;

    @JsonProperty("activeAgents")
    private int activeAgents;

    public State(int tick, List<UserState> userStates, int activeAgents) {
        this.tick = tick;
        this.userStates = userStates;
        this.activeAgents = activeAgents;
    }

    public State(int tick, List<UserState> users) {
        this.tick = tick;
        this.userStates = users;
        calculateActiveAgents();
    }

    public State() {}

    public void calculateActiveAgents() {
        this.activeAgents = this.userStates.stream()
                .reduce(0, (totalActive, user) -> totalActive + (user.hasData() ? 1 : 0), Integer::sum);
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

    public double getDisseminationFactor() {
        long agentsWithData = this.userStates.stream()
                .filter(UserState::hasData)
                .count();

        return agentsWithData / (double) this.userStates.size();
    }

    @Override
    public String toString() {
        return this.userStates.toString();
    }
}
