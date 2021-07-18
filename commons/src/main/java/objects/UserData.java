package objects;

import constants.Commands;

public class UserData extends NetworkMessage{

    private String userName;
    private String userPassword;

    public UserData(String userName, String userPassword) {
        super(Commands.AUTHORISATION);
        this.userName = userName;
        this.userPassword = userPassword;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "userName='" + userName + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
