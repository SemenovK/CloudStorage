package objects;

import constants.Commands;

import java.io.Serializable;
import java.util.Objects;

public class FileData implements Serializable {

    private String fileName;
    private long fileSize;
    private boolean isFolder;

    public String getFileName() {
        return fileName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileData fileData = (FileData) o;
        return fileSize == fileData.fileSize && isFolder == fileData.isFolder && fileName.equals(fileData.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName, fileSize, isFolder);
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
