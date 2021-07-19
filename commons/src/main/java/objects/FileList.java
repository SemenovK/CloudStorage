package objects;

import filesystem.FileInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FileList implements Serializable {
    List<FileInfo> filesList;

    @Override
    public String toString() {
        return "FileList{" +
                "filesList=" + filesList +
                '}';
    }

    public FileList(List<FileInfo> list) {
        filesList = new ArrayList<>();
        filesList.addAll(list);


    }

    public List<FileInfo> getFilesList() {
        return filesList;
    }
}
