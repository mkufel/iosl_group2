package dissemination;

import common.UserState;

public class UserStatePair {
    private UserState sender;

    private UserState receiver;

    public UserStatePair(UserState sender, UserState receiver) {
        this.sender = sender;
        this.receiver = receiver;
    }

    public UserState getSender() {
        return sender;
    }

    public UserState getReceiver() {
        return receiver;
    }
}
