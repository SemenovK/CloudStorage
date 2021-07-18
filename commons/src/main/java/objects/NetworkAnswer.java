package objects;

import constants.Commands;

import java.io.Serializable;

public class NetworkAnswer<T> extends NetworkMessage implements Serializable {
    private Commands questionMessageType;
    private T answer;

    @Override
    public String toString() {
        return "NetworkAnswer{" +
                "questionMessageType=" + questionMessageType +
                ", answer='" + answer + '\'' +
                '}';
    }

    public NetworkAnswer() {
        super(Commands.NETWORK_ANSWER);
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
}
