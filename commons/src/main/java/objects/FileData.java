package objects;

import constants.Commands;

import java.io.Serializable;

public class FileData implements Serializable {

    private String fileName;
    private long fileSize;
    private boolean isFolder;

    public String getFileName() {
        return fileName;
    }

    @Override
    public String toString() {
        return "FileData{" +
                "fileName='" + fileName + '\'' +
                ", fileSize=" + fileSize +
                ", isFolder=" + isFolder +
                '}';
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
}
