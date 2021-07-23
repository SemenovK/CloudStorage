package network;

import constants.Commands;
import constants.Status;

import java.io.Serializable;
import java.util.UUID;

public class NetworkMessage<T> implements Serializable {
    private Commands messagePurpose;
    private String extraInfo;
    private Status status;
    private UUID uid;
    private T content;

    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "NetworkMessage{" +
                "messagePurpose=" + messagePurpose +
                ", extraInfo='" + extraInfo + '\'' +
                ", status=" + status +
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
