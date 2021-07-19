package filesystem;


import objects.FileData;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FileNavigator implements Serializable {

    final private Path startingPoint;
    private Path currentFolder;
    public FileNavigator() {
        startingPoint = Paths.get("C:","TMP");
        currentFolder = startingPoint;
    }
    public FileNavigator(URI initFolder) {
        startingPoint = Paths.get(initFolder);
        currentFolder = startingPoint;
    }

    public List<FileInfo> getFilesListFromCurrent() throws IOException {
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (Path p: Files.list(currentFolder).collect(Collectors.toList())) {
            fileInfoList.add(new FileInfo(p));
        }
        return fileInfoList;
    }

    public void goInto(String foldername){
        currentFolder = Paths.get(currentFolder.toString(), foldername);
    }
    public void goUp(){
        System.out.println(currentFolder);
        System.out.println(startingPoint);
        if(currentFolder.equals(startingPoint))
            return;

        currentFolder = currentFolder.getParent();
    }

    public boolean isOnTop(){
        return startingPoint.equals(currentFolder);
    }


    public void createAndWriteToFileOnCurrent(String fileName, byte[] fileContent) {
        Path newFile =  Paths.get(currentFolder.toString(), fileName);
        try {
            Files.write(newFile, fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}