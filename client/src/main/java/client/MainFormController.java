package client;

import constants.Commands;
import filesystem.DiskInfo;
import filesystem.FileInfo;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import objects.FileData;
import objects.NetworkMessage;
import objects.UserData;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class MainFormController implements Initializable {

    public EventHandler<WindowEvent> getCloseWindowEvent() {
        return closeWindowEvent;
    }

    private EventHandler<WindowEvent> closeWindowEvent = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent windowEvent) {
            if(WindowEvent.WINDOW_CLOSE_REQUEST.equals(windowEvent.getEventType())){
                if(client!=null){
                    client.close();
                }
            }


        }
    };

    private Client client;

    public Client getClient() {
        return client;
    }

    @FXML
    private ComboBox<DiskInfo> diskComboBox;

    @FXML
    private TableView<FileInfo> filesTable;

    @FXML
    private TextField currentPath;
    @FXML
    private MenuItem miConnect;
    @FXML
    private MenuItem miDisconnect;
    @FXML
    private ListView<String> eventsList;
    @FXML
    private TableView<FileData> filesOnServerTable;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        miDisconnect.setDisable(true);
        List<File> drives = new ArrayList<>();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            drives.add(p.toFile());
        }
        ;

        diskComboBox.getItems().clear();

        for (File drive : drives) {
            try {
                diskComboBox.getItems().add(new DiskInfo(drive));
            } catch (IOException e) {
                throw new RuntimeException("Unable to collect drives");
            }
        }
        diskComboBox.setMaxWidth(70);

        diskComboBox.setButtonCell(new ListCell<DiskInfo>() {
            @Override
            protected void updateItem(DiskInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null || !empty) {
                    //setText(item.getDiskName());
                    setText(item.getDiskAbsPath());
                }

            }
        });
        diskComboBox.setCellFactory(row -> new ComboBoxListCell<DiskInfo>() {
            @Override
            public void updateItem(DiskInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null || !empty) {
                    setText(item.getDiskAbsPath() + " (" + item.getDiskName() + ")");
                }
            }

        });

        TableColumn<FileInfo, String> fileTypeColumn = new TableColumn<>();
        fileTypeColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileType().getName()));
        fileTypeColumn.setPrefWidth(25);


        TableColumn<FileInfo, String> fileNameColumn = new TableColumn<>("Name");
        fileNameColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(250);


        TableColumn<FileInfo, Long> fileSizeColumn = new TableColumn<>("Size");
        fileSizeColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getFileSize()));
        fileSizeColumn.setPrefWidth(150);


        fileSizeColumn.setCellFactory(column -> new TableCell<FileInfo, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String str = item.toString();

                    if (item == -1L) {
                        str = "[DIR]";
                    } else {
                        str = String.format("%,d bytes", item);
                    }
                    setText(str);
                }

            }
        });

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        TableColumn<FileInfo, String> lastModifyDateColumn = new TableColumn<>("Last Modify Date");
        lastModifyDateColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getLastModifiedDate().format(dtf)));
        lastModifyDateColumn.setPrefWidth(150);
        filesTable.getColumns().addAll(fileTypeColumn, fileNameColumn, fileSizeColumn, lastModifyDateColumn);

        TableColumn<FileData, String> fileNameServerColumn = new TableColumn<>("Name");
        fileNameServerColumn.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().getFileName()));
        fileNameServerColumn.setPrefWidth(150);


        TableColumn<FileData, Long> fileSizeServerColumn = new TableColumn<>("Size");
        fileSizeServerColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getFileSize()));
        fileSizeServerColumn.setPrefWidth(30);


        fileSizeServerColumn.setCellFactory(column -> new TableCell<FileData, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String str = item.toString();

                    if (item == -1L) {
                        str = "[DIR]";
                    } else {
                        str = String.format("%,d bytes", item);
                    }
                    setText(str);
                }

            }
        });
        filesOnServerTable.getColumns().addAll(fileNameServerColumn, fileSizeServerColumn);
        updateFileList(Paths.get(System.getProperty("user.home")).toAbsolutePath().normalize());

    }


    private void updateFileList(Path path) {
        synchronizePathData(path);
        try {
            List<Path> pp = Files.list(path).collect(Collectors.toList());
            for (Path p : Files.list(path).collect(Collectors.toList())) {
                filesTable.getItems().add(new FileInfo(p));
            }
            filesTable.sort();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING, "Can't refresh files list", ButtonType.OK);
            alert.showAndWait();
        }
    }

    private void synchronizePathData(Path path) {
        currentPath.clear();
        currentPath.setText(path.normalize().toAbsolutePath().toString());
        filesTable.getItems().clear();
        for (DiskInfo di : diskComboBox.getItems()) {
            if (di.getFile().toPath().getRoot().equals(path.getRoot())) {
                diskComboBox.setValue(di);
            }
        }

    }

    public void goToPathClick(ActionEvent actionEvent) {
        Path p = Paths.get(currentPath.getText());
        updateFileList(p.normalize().toAbsolutePath());
    }

    public void moveupClick(ActionEvent actionEvent) {
        Path path = Paths.get(currentPath.getText()).getParent();
        if (path != null)
            updateFileList(path);
    }

    public void onFilesTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            FileInfo fi = filesTable.getSelectionModel().getSelectedItem();
            if (fi.getFileType() == FileInfo.FileType.DIRECTORY) {
                updateFileList(fi.getFilePath());
            }
        }
    }

    public void onFilesTableKeyPress(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            FileInfo fi = filesTable.getSelectionModel().getSelectedItem();
            if (fi.getFileType() == FileInfo.FileType.DIRECTORY) {
                updateFileList(fi.getFilePath());
            }
        } else if (keyEvent.getCode() == KeyCode.BACK_SPACE) {
            moveupClick(new ActionEvent());
        }
    }

    public void miConnectClick(ActionEvent actionEvent) {
        connectAndAuth();
        miConnect.setDisable(true);
        miDisconnect.setDisable(false);
        eventsList.getItems().add("Connected.");


    }

    public void miDisconnectClick(ActionEvent actionEvent) {
        client.getMysocketChannel().close();
        miConnect.setDisable(false);
        miDisconnect.setDisable(true);
        eventsList.getItems().add("Disconected.");
    }

    public void testButtonClick(ActionEvent actionEvent) {
        client.SendObject(new NetworkMessage(Commands.GET_FILE_LIST));
        filesOnServerTable.getItems().clear();
        client.getHandler().setFileListContainer(filesOnServerTable);

    }

    private void connectAndAuth() {
        client = new Client();
        authorisationWindowShow();
    }

    public void authorisationWindowShow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./AuthWindow.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            //    stage.setOpacity(1);
            stage.setTitle("Log in");
            stage.setScene(new Scene(root, 350, 160));
            stage.initModality(Modality.WINDOW_MODAL);
            AuthWindowController loginWindow = loader.getController();
            loginWindow.setParentController(this);
            stage.showAndWait();
            UserData ud = loginWindow.getCollectedData();
            if (ud != null && client != null) {
                client.getMysocketChannel().writeAndFlush(ud);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    public void exitFrom(ActionEvent actionEvent) {
        Platform.exit();
    }

    public void onFilesServerTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            FileData fd = filesOnServerTable.getSelectionModel().getSelectedItem();
            if (fd.isFolder()) {
                NetworkMessage nm = new NetworkMessage(Commands.GET_FILE_LIST);
                nm.setExtraInfo(fd.getFileName());
                client.SendObject(nm);
                filesOnServerTable.getItems().clear();
                client.getHandler().setFileListContainer(filesOnServerTable);
            }
        }
    }
}
