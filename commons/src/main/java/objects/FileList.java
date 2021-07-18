package objects;

import filesystem.FileInfo;

import java.io.Serializable;
import java.util.List;

public class FileList extends NetworkAnswer implements Serializable {
    List<FileInfo> filesList;

    @Override
    public String toString() {
        return "FileList{" +
                "filesList=" + filesList +
                '}';
    }

    public FileList(List<FileInfo> list) {
        super();
        this.filesList = list;

    }

    public List<FileInfo> getFilesList() {
        return filesList;
    }
}
