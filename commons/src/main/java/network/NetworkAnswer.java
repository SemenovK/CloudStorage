package network;

import constants.Commands;

public class NetworkAnswer<T> extends NetworkMessage {
    private Commands questionMessageType;
    private T answer;
    private int totalParts;
    private int currentPart;

    public int getTotalParts() {
        return totalParts;
    }

    public int getCurrentPart() {
        return currentPart;
    }

    public NetworkAnswer() {
        super(Commands.NETWORK_ANSWER);
        this.totalParts = 1;
    }

    public void setCurrentPart(int currentPart) {
        if(currentPart>0 && currentPart<totalParts)
            this.currentPart = currentPart;
    }

    public NetworkAnswer(int partsNum) {
        super(Commands.NETWORK_ANSWER);
        this.totalParts = partsNum;
    }

    public Commands getQuestionMessageType() {
        return questionMessageType;
    }

    @Override
    public String toString() {
        return "NetworkAnswer{" +
                "questionMessageType=" + questionMessageType +
                ", answer=" + answer +
                ", totalParts=" + totalParts +
                ", currentPart=" + currentPart +
                '}';
    }

    public void setQuestionMessageType(Commands questionMessageType) {
        this.questionMessageType = questionMessageType;
    }

    public T getAnswer() {
        return answer;
    }

    public void setAnswer(T answer) {
        this.answer = answer;
    }
}