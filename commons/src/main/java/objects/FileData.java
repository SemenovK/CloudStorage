package objects;

import constants.Commands;

import java.io.Serializable;
import java.util.Objects;

public class FileData implements Serializable {

    private int folderID;
    private String fileName;
    private long fileSize;
    private boolean isFolder;
    private boolean sharedFolder;
    private boolean isVirtual;

    public boolean isVirtual() {
        return isVirtual;
    }

    public void setVirtual(boolean virtual) {
        isVirtual = virtual;
    }

    public boolean isSharedFolder() {
        return sharedFolder;
    }

    public void setSharedFolder(boolean sharedFolder) {
        this.sharedFolder = sharedFolder;
    }

    public String getFileName() {
        return fileName;
    }


    public long getFileSize() {
        return fileSize;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public FileData(String fileName, long fileSize, boolean isFolder) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.isFolder = isFolder;
    }

    public int getFolderID() {
        return folderID;
    }

    public FileData(String fileName, int folderID) {
        this.fileName = fileName;
        this.fileSize = -1;
        this.folderID = folderID;
        this.isFolder = true;
        this.isVirtual = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return folderID == fileData.folderID && fileSize == fileData.fileSize && isFolder == fileData.isFolder && sharedFolder == fileData.sharedFolder && isVirtual == fileData.isVirtual && fileName.equals(fileData.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(folderID, fileName, fileSize, isFolder, sharedFolder, isVirtual);
    }

    @Override
    public String toString() {
        return "FileData{" +
                "folderID=" + folderID +
                ", fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", isFolder=" + isFolder +
                ", sharedFolder=" + sharedFolder +
                ", isVirtual=" + isVirtual +
                '}';
    }
}
