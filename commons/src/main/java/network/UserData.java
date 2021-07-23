package network;

import constants.Commands;

public class UserData extends NetworkMessage{


    private String userLogin;
    private String userPassword;
    private int userID;

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public UserData(String userLogin, String userPassword) {
        super(Commands.AUTHORISATION);
        this.userLogin = userLogin;
        this.userPassword = userPassword;
    }

    @Override
    public String toString() {
        return "UserData{" +
                "userName='" + userLogin + '\'' +
                ", userPassword='" + userPassword + '\'' +
                '}';
    }

    public String getUserLogin() {
        return userLogin;
    }

    public String getUserPassword() {
        return userPassword;
    }
}
