package objects;

import java.io.Serializable;

public class UserInfo implements Serializable {
    private int userId;
    private String userName;
    private boolean thisUserIsCurrent;

    public UserInfo(int userId, String userName, boolean thisUserIsCurrent) {
        this.userId = userId;
        this.userName = userName;
        this.thisUserIsCurrent = thisUserIsCurrent;
    }

    public int getUserId() {
        return userId;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", thisUserIsCurrent=" + thisUserIsCurrent +
                '}';
    }

    public String getUserName() {
        return userName;
    }

    public boolean isThisUserIsCurrent() {
        return thisUserIsCurrent;
    }
}
