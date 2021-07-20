package filesystem;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileToWrite {
    Path filePath;
    String fileName;
    byte[] bytes;
    boolean doWrite;
    UUID uuid;

    @Override
    public String toString() {
        return "FileToWrite{" +
                "filePath=" + filePath +
                ", fileName='" + fileName + '\'' +
                ", doWrite=" + doWrite +
                ", uuid=" + uuid +
                '}';
    }

    public UUID getUuid() {
        return uuid;
    }

    public FileToWrite(UUID uuid, Path filePath, String fileName, byte[] bytes) {
        this.uuid = uuid;
        this.filePath = filePath;
        this.fileName = fileName;
        this.bytes = bytes;
        this.doWrite = !Paths.get(filePath.toString(), fileName).toFile().exists();
    }

    public String getFileName() {
        return fileName;
    }

    public void setDoWrite(boolean doWrite) {
        this.doWrite = doWrite;
    }

}
