package dissemination;

import common.UserState;

/**
 * Represents two Users who can exchange data with each other.
 */
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
