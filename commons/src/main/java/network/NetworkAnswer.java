package network;

import constants.Commands;

public class NetworkAnswer<T> extends NetworkMessage {
    private Commands questionMessageType;
    private T answer;
    private int currentPart;


    public int getCurrentPart() {
        return currentPart;
    }

    public NetworkAnswer() {
        super(Commands.NETWORK_ANSWER);
    }

    public void setCurrentPart(int currentPart) {
            this.currentPart = currentPart;
    }

    public Commands getQuestionMessageType() {
        return questionMessageType;
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

    @Override
    public String toString() {
        return "NetworkAnswer{" +
                "questionMessageType=" + questionMessageType +
                ", answer=" + answer +
                ", currentPart=" + currentPart +
                '}';
    }


}
