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
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import network.FileContent;
import objects.FileData;
import network.NetworkMessage;
import network.UserData;

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

    private Thread monitorthread;

    public EventHandler<WindowEvent> getCloseWindowEvent() {
        return closeWindowEvent;
    }

    private EventHandler<WindowEvent> closeWindowEvent = new EventHandler<WindowEvent>() {
        @Override
        public void handle(WindowEvent windowEvent) {
            if (WindowEvent.WINDOW_CLOSE_REQUEST.equals(windowEvent.getEventType())) {
                if (client != null) {
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
    @FXML
    private Pane buttonsPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setConnectedMode(false);
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
        fileSizeServerColumn.setPrefWidth(80);


        fileSizeServerColumn.setCellFactory(column -> new TableCell<FileData, Long>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else {
                    String str = item.toString();

                    if (item == -1L && !str.equals("..")) {
                        str = "[DIR]";
                    } else if (item == -2L) {
                        str = "[Move up]";
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

    public void moveUpClick(ActionEvent actionEvent) {
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
            moveUpClick(new ActionEvent());
        }
    }

    public void miConnectClick(ActionEvent actionEvent) {
        connectAndAuth();
    }

    public void miDisconnectClick(ActionEvent actionEvent) {
        disconnect();

    }

    public synchronized void refreshButtonClick(ActionEvent actionEvent) {
        if (client == null) {
            setConnectedMode(false);
            return;
        }
        setConnectedMode(client.isConnected());
        Path path = Paths.get(currentPath.getText());
        if (path != null)
            updateFileList(path);
        client.SendObject(new NetworkMessage(Commands.GET_FILE_LIST));
        filesOnServerTable.getItems().clear();
        client.getHandler().setFileListContainer(filesOnServerTable);


    }

    private void connectAndAuth() {
        authorisationWindowShow();
    }

    public void authorisationWindowShow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./AuthWindow.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Log in");
            stage.setScene(new Scene(root, 350, 160));
            stage.initModality(Modality.WINDOW_MODAL);
            AuthWindowController loginWindow = loader.getController();
            loginWindow.setParentController(this);
            client = new Client();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            UserData ud = loginWindow.getCollectedData();
            if (ud == null) {
                disconnect();
                return;
            }

            if (ud != null && client != null) {
                client.getMySocketChannel().writeAndFlush(ud);
                setConnectedMode(true);
                addEventToList("Connected");

                Platform.runLater(() -> {
                    while (true) {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (client != null) {
                            if (client.isConnected()) {
                                break;
                            }
                        }
                    }
                    refreshButtonClick(new ActionEvent());
                });

            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private void disconnect() {
        setConnectedMode(false);
        if (client != null) {
            if (client.isConnected()) {
                client.setConnected(false);
                addEventToList("Disconnected");
            }

            client.close();
            client = null;


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

    public void addToCloud(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            List<FileInfo> fi = filesTable.getSelectionModel().getSelectedItems();

            for (FileInfo f : fi) {
                if (!f.isFolder()) {
                    FileContent fc = new FileContent(f.getFileName(), (int) f.getFileSize());
                    try {
                        fc.setFileContent(Files.readAllBytes(f.getFilePath()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    client.SendObject(fc);
                    addEventToList("File " + f.getFileName() + " has been sent to the Cloud.");
                } else {
                    addEventToList("Unable to send folder " + f.getFileName() + " to the Cloud.");
                }

            }
        });

    }

    private void setConnectedMode(boolean mode) {
        buttonsPane.setDisable(!mode);
        miDisconnect.setDisable(!mode);
        miConnect.setDisable(mode);
    }

    private void addEventToList(String message) {
        eventsList.getItems().add(message);
        eventsList.scrollTo(eventsList.getItems().size());

    }
}
