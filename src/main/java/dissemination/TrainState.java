package dissemination;

import common.UserState;

import java.util.List;

public class TrainState {
    List<UserState> passengers;

    public TrainState(List<UserState> passengers) {
        this.passengers = passengers;
    }

    public List<UserState> getPassengers() {
        return passengers;
    }

    public void setPassengers(List<UserState> passengers) {
        this.passengers = passengers;
    }
}
