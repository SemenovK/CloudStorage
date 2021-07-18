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

    final Path startingPoint;
    private Path currentFolder;

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


    public byte[] getFileContentFromCurrent(String fileName) throws IOException {
        Path filePath = Paths.get(currentFolder.toString(), fileName);
        System.out.println(filePath);
        if (!filePath.toFile().exists()) {
            return ("File " + fileName + " doesn't exist.\n").getBytes(StandardCharsets.UTF_8);
        } else {
            return Files.readAllBytes(filePath);
        }

    }
}