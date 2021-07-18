package filesystem;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FileInfo implements Serializable {
    public enum FileType {
        FILE("F"), DIRECTORY("D");
        private String name;

        FileType(String str) {
            name = str;
        }

        public String getName() {
            return name;
        }

    }

    private String fileName;
    private Path filePath;
    private long fileSize;
    private LocalDateTime lastModifiedDate;
    private FileType fileType;
    private File file;


    public FileInfo(Path path) {

            this.file = new File(String.valueOf(path));
            this.filePath = path;
            this.fileName = path.normalize().getFileName().toString();
            this.fileType = Files.isDirectory(path) ? FileType.DIRECTORY : FileType.FILE;

            try {
                this.fileSize = this.fileType == FileType.DIRECTORY ? -1 : Files.size(path);
            } catch (IOException e) {
                this.fileSize = 0L;
            }

            try {
                this.lastModifiedDate = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
            } catch (IOException e) {
            this.lastModifiedDate = LocalDateTime.of(1900,01,1,0,0,0,0);
            }
    }


    public long getFileSize() {
        return fileSize;
    }

    public FileType getFileType() {
        return fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public Path getFilePath() {
        return filePath;
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

}


