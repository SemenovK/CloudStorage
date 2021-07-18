package objects;

import constants.Commands;

import java.io.Serializable;

public class NetworkMessage implements Serializable {
    private Commands messagePurpose;
    private String extraInfo;

    public Commands getMessagePurpose() {
        return messagePurpose;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public NetworkMessage(Commands command){
        messagePurpose = command;
    }
}
