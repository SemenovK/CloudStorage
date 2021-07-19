package network;

import constants.Commands;

import java.io.Serializable;
import java.util.UUID;

public class NetworkMessage implements Serializable {
    private Commands messagePurpose;
    private String extraInfo;
    private UUID uid;

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "messagePurpose=" + messagePurpose +
                ", extraInfo='" + extraInfo + '\'' +
                ", uid=" + uid +
                '}';
    }

    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }

    public Commands getMessagePurpose() {
        return messagePurpose;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public NetworkMessage(Commands command){
        messagePurpose = command;
    }
}
