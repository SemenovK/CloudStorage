package network;

import constants.Commands;
import network.NetworkMessage;

public class FileContent extends NetworkMessage {
    private String fileName;
    private byte[] fileContent;

    public byte[] getFileContent() {
        return fileContent;
    }

    @Override
    public String toString() {
        return "FileContent{" +
                "fileName='" + fileName + '\'' +
                '}';
    }

    public void setFileContent(byte[] fileContent) {
        this.fileContent = fileContent;
    }

    public FileContent(String fileName, int fileSize) {
        super(Commands.FILE_DATA);
        this.fileName = fileName;
        this.fileContent = new byte[fileSize];

    }

    public String getFileName() {
        return fileName;
    }
}
