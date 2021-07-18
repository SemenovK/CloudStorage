package objects;

import constants.Commands;

public class FileData extends NetworkMessage{

    private String fileName;
    private String fileSize;
    private boolean isFolder;


    public FileData() {
        super(Commands.FILE_DATA);
    }
}
