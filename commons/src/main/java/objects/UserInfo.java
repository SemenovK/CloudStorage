package objects;

import java.io.Serializable;
import java.util.Objects;

public class UserInfo implements Serializable {
    private int userId;
    private String userName;
    private boolean thisUserIsCurrent;

    public UserInfo(int userId, String userName, boolean thisUserIsCurrent) {
        this.userId = userId;
        this.userName = userName;
        this.thisUserIsCurrent = thisUserIsCurrent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return userId == userInfo.userId && thisUserIsCurrent == userInfo.thisUserIsCurrent && Objects.equals(userName, userInfo.userName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, userName, thisUserIsCurrent);
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
    public int getUserId() {
        return userId;
    }
}
