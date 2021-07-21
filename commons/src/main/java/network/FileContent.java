package network;

import constants.Commands;
import network.NetworkMessage;

public class FileContent extends NetworkMessage {
    private String fileName;
    private String filePath;
    private byte[] fileContent;

    public byte[] getFileContent() {
        return fileContent;
    }

    @Override
    public String toString() {
        return "FileContent{" +
                "fileName='" + fileName + '\'' +
                ", filePath='" + filePath + '\'' +
                '}';
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }


    public FileContent(String fileName, int fileSize) {
        super(Commands.FILE_DATA);
        this.fileName = fileName;
        this.fileContent = new byte[fileSize];
        this.filePath = "\\";
    }

    public String getFilePath() {
        return filePath;
    }

    public FileContent(String filePath, String fileName, int fileSize) {
        super(Commands.FILE_DATA);
        this.fileName = fileName;
        this.fileContent = new byte[fileSize];
        this.filePath = filePath;

    }

    public String getFileName() {
        return fileName;
    }
}
