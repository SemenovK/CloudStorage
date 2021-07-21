package filesystem;


import lombok.extern.slf4j.Slf4j;
import network.FileContent;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.*;
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

    public FileNavigator(UUID uid, int userID) {
        startingPoint = Paths.get("C:", "TMP",Integer.toString(userID));
        if(!startingPoint.toFile().exists()){
            startingPoint.toFile().mkdirs();
        }
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
                    } else {
                        filesAwait.add(filesQueue.removeFirst());
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

    public void goInto(String folderName) {
        currentFolder = Paths.get(currentFolder.toString(), folderName);
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
            if(!Files.exists(path)){
                path.toFile().mkdirs();
            }
            Files.write(newFile, fileContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public synchronized void putFileToQueue(String filename, byte[] fileContent) {
        filesQueue.add(new FileToWrite(this.uuid, this.currentFolder, filename, fileContent));
    }
    public synchronized void putFileToQueue(FileContent fc) {
        filesQueue.add(new FileToWrite(this.uuid, Paths.get(this.currentFolder.toString(),fc.getFilePath()), fc.getFileName(), fc.getFileContent()));
    }

    public void putFileToQueue(FileToWrite f) {
        filesQueue.add(f);
    }

    public void createFolder(String folderName) {
        Path newFile = Paths.get(currentFolder.toString(), folderName);
        if (!newFile.toFile().exists()) {
            newFile.toFile().mkdir();
        }
    }

    public void deleteFileOrFolder(String fileOrFolderName) {
        Path file = Paths.get(currentFolder.toString(), fileOrFolderName);
        System.out.println(file.toString());
        File fileHandle = file.toFile();
        if (fileHandle.exists()) {
            try {
                if (fileHandle.isDirectory()) {
                    Files.walk(file).sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
                } else if (fileHandle.isFile()) {
                    Files.delete(file);
                }
            } catch (IOException e) {
                log.error("Error during delete - "+file.toString());

            }
        }
    }

    public Path getCurrentFolder() {
        return currentFolder;
    }


}