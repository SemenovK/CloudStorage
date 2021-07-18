package filesystem;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.IOException;

public class DiskInfo{
    String diskName;
    String diskAbsPath;
    File file;


    public File getFile() {
        return file;
    }

    public DiskInfo(File f) throws IOException {
        FileSystemView fsv = FileSystemView.getFileSystemView();
        this.file = f;
        diskName = fsv.getSystemDisplayName(f);
        diskAbsPath = f.getPath();


    }

    public String getDiskName() {
        return diskName;
    }

    public String getDiskAbsPath() {
        return diskAbsPath;
    }
}