package filesystem;


import lombok.extern.slf4j.Slf4j;
import objects.FileData;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class FileNavigator implements Serializable {



    LinkedList<FileToWrite> filesQueue;
    static LinkedList<FileToWrite> filesAwait = new LinkedList<>();
    Thread monitorThread;

    final private Path startingPoint;
    private Path currentFolder;
    private UUID uuid;

    public UUID getUuid() {
        return uuid;
    }

    public FileNavigator(UUID uid) {
        startingPoint = Paths.get("C:", "TMP");
        uuid = uid;
        init();
    }


    public static LinkedList<FileToWrite> getFilesAwait() {
        return filesAwait;
    }

    private void init() {
        filesQueue = new LinkedList<>();
        currentFolder = startingPoint;
        monitorThread = new Thread(() -> {
            while (true) {

                if (!filesQueue.isEmpty()) {
                    FileToWrite fileToWrite = filesQueue.peek();
                    System.out.println(fileToWrite);
                    if (fileToWrite.doWrite) {
                        fileToWrite = filesQueue.removeFirst();
                        createAndWriteToFileOnCurrent(fileToWrite.filePath, fileToWrite.fileName, fileToWrite.bytes);
                        log.info("File " + fileToWrite.fileName + " recieved");
                    } else {
                        log.info("File " + fileToWrite.fileName + " exists");
                        filesAwait.add(filesQueue.removeFirst());
                        System.out.println("Waiting-"+filesAwait.size());
                    }
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
        monitorThread.setDaemon(true);
        monitorThread.start();
    }

    public List<FileInfo> getFilesListFromCurrent() throws IOException {
        List<FileInfo> fileInfoList = new ArrayList<>();
        for (Path p : Files.list(currentFolder).collect(Collectors.toList())) {
            fileInfoList.add(new FileInfo(p));
        }
        return fileInfoList;
    }

    public void goInto(String foldername) {
        currentFolder = Paths.get(currentFolder.toString(), foldername);
    }

    public void goUp() {
        System.out.println(currentFolder);
        System.out.println(startingPoint);
        if (currentFolder.equals(startingPoint))
            return;

        currentFolder = currentFolder.getParent();
    }

    public boolean isOnTop() {
        return startingPoint.equals(currentFolder);
    }


    public synchronized void createAndWriteToFileOnCurrent(Path path, String fileName, byte[] fileContent) {
        Path newFile = Paths.get(path.toString(), fileName);
        try {
            Files.write(newFile, fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void putFileToQueue(String filename, byte[] fileContent){
        filesQueue.add(new FileToWrite(this.uuid, this.currentFolder, filename, fileContent));
    }

    public void putFileToQueue(FileToWrite f) {
        filesQueue.add(f);
    }
}