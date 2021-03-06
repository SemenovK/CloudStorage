package objects;

import java.io.Serializable;

public class ShareFolderTo implements Serializable {
    private String folderName;
    private int toUserId;

    public ShareFolderTo(String folderName, int toUserId) {
        this.folderName = folderName;
        this.toUserId = toUserId;
    }

    public String getFolderName() {
        return folderName;
    }
    public int getToUserId() {
        return toUserId;
    }

    @Override
    public String toString() {
        return "ShareFolderTo{" +
                "folderName='" + folderName + '\'' +
                ", toUserId=" + toUserId +
                '}';
    }


}
