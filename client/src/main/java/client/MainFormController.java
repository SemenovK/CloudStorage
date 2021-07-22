package client;

import constants.Commands;
import constants.Status;
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
import lombok.extern.slf4j.Slf4j;
import objects.FileData;
import network.NetworkMessage;
import network.UserData;
import objects.UserInfo;

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

@Slf4j
public class MainFormController implements Initializable {

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
    private boolean sharedMode;
    private Scene scene;

    public void setScene(Scene scene) {
        this.scene = scene;
    }

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
    @FXML
    private TextField tfServerPath;
    @FXML
    private ComboBox<UserInfo> cbSharedFrom;
    @FXML
    private ContextMenu contextMenuServer;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setConnectedMode(false);
        List<File> drives = new ArrayList<>();
        for (Path p : FileSystems.getDefault().getRootDirectories()) {
            drives.add(p.toFile());
        }
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
        fileNameColumn.setCellValueFactory(param -> new SimpleObjectProperty(param.getValue().getFileName()));
        fileNameColumn.setPrefWidth(250);

        fileNameColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else if (!empty && item != null) {
                    FileInfo fi = this.getTableRow().getItem();
                    if (fi != null) {
                        if (this.getTableRow().getItem().isFolder()) {
                            setStyle("-fx-font-weight: bold;");
                        } else {
                            setStyle("");
                        }
                        setText(this.getTableRow().getItem().getFileName());
                    }

                }


            }
        });

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
        fileNameServerColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setStyle("");
                } else if (!empty && item != null) {
                    FileData fi = this.getTableRow().getItem();
                    if (fi != null) {
                        if (fi.isFolder()) {
                            if (fi.isSharedFolder()) {
                                setStyle("-fx-text-fill: blue;-fx-font-weight: bold;");

                            } else {
                                setStyle("-fx-font-weight: bold");
                            }
                        } else {
                            setStyle("");
                        }
                        setText(this.getTableRow().getItem().getFileName());
                    }

                }


            }
        });

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


        cbSharedFrom.setButtonCell(new ListCell<UserInfo>() {
            @Override
            protected void updateItem(UserInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null || !empty) {
                    if (!item.isThisUserIsCurrent()) {
                        setText("[Shared from " + item.getUserName() + "]");
                    } else {
                        setText(item.getUserName());
                    }


                } else {
                    setText("");
                }

            }
        });
        cbSharedFrom.setCellFactory(row -> new ComboBoxListCell<UserInfo>() {
            @Override
            public void updateItem(UserInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null || !empty) {
                    if (!item.isThisUserIsCurrent()) {
                        setText("[Shared from " + item.getUserName() + "]");
                    } else {
                        setText(item.getUserName());
                    }

                } else {
                    setText("");
                }
            }

        });
        cbSharedFrom.getItems().clear();
        tfServerPath.setText(cbSharedFrom.getButtonCell().getText());

    }


    private void updateFileList(Path path) {
        synchronizePathData(path);
        try {
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
        if (client != null) {
            client.setPathToSave(Paths.get(currentPath.getText()));
        }

    }

    @FXML
    public void goToPathClick(ActionEvent actionEvent) {
        Path p = Paths.get(currentPath.getText());
        updateFileList(p.normalize().toAbsolutePath());

    }

    @FXML
    public void moveUpClick(ActionEvent actionEvent) {
        Path path = Paths.get(currentPath.getText()).getParent();
        if (path != null)
            updateFileList(path);

    }

    @FXML
    public void onFilesTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            FileInfo fi = filesTable.getSelectionModel().getSelectedItem();
            if (fi.getFileType() == FileInfo.FileType.DIRECTORY) {
                updateFileList(fi.getFilePath());
            }
        }
    }

    @FXML
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

    @FXML
    public void miConnectClick(ActionEvent actionEvent) {
        connectAndAuth();
    }

    @FXML
    public void miDisconnectClick(ActionEvent actionEvent) {
        disconnect();

    }

    @FXML
    public synchronized void refreshButtonClick(ActionEvent actionEvent) {
        if (client == null) {
            setConnectedMode(false);
            return;
        }
        setConnectedMode(client.isConnected());
        Path path = Paths.get(currentPath.getText());
        updateFileList(path);
        filesOnServerTable.getItems().clear();
        client.getHandler().setFileListContainer(filesOnServerTable);
        UserInfo ui = cbSharedFrom.getSelectionModel().getSelectedItem();
        client.askFilesList(ui);
        client.askUserList();
        client.askFriendsList();


        cbSharedFrom.getItems().clear();
        cbSharedFrom.getItems().addAll(client.getFriendsList());
        cbSharedFrom.setValue(ui);
        tfServerPath.setText("[USER_HOME]");


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

            if (client != null) {
                client.getMySocketChannel().writeAndFlush(ud);

                Platform.runLater(() -> {
                    while (true) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        if (client != null) {
                            if (client.isAuthorised()) {
                                setConnectedMode(true);
                                ((Stage) scene.getWindow()).setTitle("Cloud storage. User [" + ud.getUserLogin() + "] connected.");
                                refreshButtonClick(new ActionEvent());
                                addEventToList("Connected");
                                break;
                            } else {
                                setConnectedMode(false);
                                addEventToList("Connection denied");

                                client.close();
                                break;
                            }
                        }
                    }


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

    @FXML
    public void onFilesServerTableClick(MouseEvent mouseEvent) {
        if (mouseEvent.getClickCount() == 2) {
            FileData fd = filesOnServerTable.getSelectionModel().getSelectedItem();
          if (fd.isFolder() && !fd.isVirtual()) {

                NetworkMessage nm = new NetworkMessage(Commands.GET_FILE_LIST);
                nm.setExtraInfo(fd.getFileName());
                client.sendObject(nm);
                filesOnServerTable.getItems().clear();
                client.getHandler().setFileListContainer(filesOnServerTable);

                if (fd.getFileSize() == -2) {
                    tfServerPath.setText(Paths.get(tfServerPath.getText()).getParent().toString());
                } else {
                    tfServerPath.setText(Paths.get(tfServerPath.getText(), fd.getFileName()).toString());
                }
            } else if((fd.isFolder() && fd.isVirtual()))

            {
                NetworkMessage nm = new NetworkMessage(Commands.GET_FILE_SHAREDLIST);
                nm.setExtraInfo(Integer.toString(fd.getFolderID()));
                client.sendObject(nm);
                filesOnServerTable.getItems().clear();
                if (fd.getFileSize() == -2) {
                    tfServerPath.setText(Paths.get(tfServerPath.getText()).getParent().toString());
                } else {
                    tfServerPath.setText(Paths.get(tfServerPath.getText(), fd.getFileName()).toString());
                }
            }


        }

    }

    @FXML
    public void addToCloud(ActionEvent actionEvent) {
        Platform.runLater(() -> {
            List<FileInfo> fi = filesTable.getSelectionModel().getSelectedItems();

            for (FileInfo f : fi) {
                client.sendFile(f);
                addEventToList("File " + f.getFileName() + " has been sent to the Cloud.");
            }
        });

    }


    private void setConnectedMode(boolean mode) {
        buttonsPane.setDisable(!mode);
        miDisconnect.setDisable(!mode);
        miConnect.setDisable(mode);
        for (MenuItem item : contextMenuServer.getItems()) {
            item.setDisable(!mode);
        }
        if (!mode && scene != null) {
            ((Stage) scene.getWindow()).setTitle("Cloud storage");
        }

    }

    private void addEventToList(String message) {
        eventsList.getItems().add(message);
        eventsList.scrollTo(eventsList.getItems().size());

    }

    @FXML
    public void downloadFromCloud() {
        Platform.runLater(() -> {
            List<FileData> fi = filesOnServerTable.getSelectionModel().getSelectedItems();

            for (FileData f : fi) {
                //  if (!f.isFolder()) {
                addEventToList("File " + f.getFileName() + " downloading attempt.");
                NetworkMessage nm = new NetworkMessage(Commands.FILE_DOWNLOAD);
                nm.setExtraInfo(f.getFileName());
                client.sendObject(nm);


            }
        });

    }

    @FXML
    public void createFolderOnServer() {
        TextInputDialog textInputDialog = new TextInputDialog();
        textInputDialog.setContentText("Enter new folder name");
        textInputDialog.showAndWait();
        String folderName = textInputDialog.getResult();
        if (folderName != null) {
            NetworkMessage nm = new NetworkMessage(Commands.CREATE_NEW_FOLDER);
            nm.setExtraInfo(folderName);
            client.sendObject(nm);
        }
    }

    @FXML
    public void deleteOnServer() {
        FileData f = filesOnServerTable.getSelectionModel().getSelectedItem();
        StringBuilder sb = new StringBuilder();
        sb.append("Are you really want to delete ").append(f.getFileName()).append(" ?");

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, sb.toString(), ButtonType.YES, ButtonType.NO);
        alert.showAndWait();
        if (alert.getResult().equals(ButtonType.YES)) {
            NetworkMessage nm = new NetworkMessage(Commands.DELETE);
            nm.setExtraInfo(f.getFileName());
            client.sendObject(nm);
        }


    }

    @FXML
    public void cbSharedFromAction(ActionEvent actionEvent) {
        UserInfo ui = cbSharedFrom.getSelectionModel().getSelectedItem();
        tfServerPath.setText(cbSharedFrom.getButtonCell().getText());
        if (ui != null) {
            if (ui.isThisUserIsCurrent()) {
                NetworkMessage nm = new NetworkMessage(Commands.GO_INTO_NORMAL_MODE);
                client.sendObject(nm);
                this.sharedMode = false;
                client.askFilesList(ui);
            } else {
                this.sharedMode = true;
                client.getSharedFoldersListFromUser(ui);
            }
        }
    }

    @FXML
    public void shareFolderToSmb(ActionEvent actionEvent) {
        showShareDialog(true);
        refreshButtonClick(new ActionEvent());

    }

    private void showShareDialog(boolean doShare) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("./UserChoose.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Users");
            stage.setScene(new Scene(root, 350, 160));
            stage.initModality(Modality.APPLICATION_MODAL);
            UserChooseController userChooseController = loader.getController();
            if (doShare) {
                userChooseController.setUsersList(client.getUsersList());
            } else {
                userChooseController.setUsersList(client.getSharedToUsersList());
            }
            userChooseController.setStage(stage);
            stage.showAndWait();

            UserInfo ui = userChooseController.getSelected();
            if (ui != null) {
                FileData fd = filesOnServerTable.getSelectionModel().getSelectedItem();
                if (fd.isFolder()) {
                    client.shareFolderTo(fd, ui, doShare ? Status.OK : Status.CANCEL);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void updateShares(MouseEvent mouseEvent) {
        cbSharedFrom.getItems().clear();
        if (!client.getFriendsList().isEmpty()) {
            cbSharedFrom.getItems().add(new UserInfo(-1, "[USER_HOME]", true));
        }

        cbSharedFrom.getItems().addAll(client.getFriendsList());
        System.out.println(client.getFriendsList());
    }

    public void unshareFolder(ActionEvent actionEvent) {
        client.askMySharesList(filesOnServerTable.getSelectionModel().getSelectedItem());
        showShareDialog(false);
        client.getSharedToUsersList().clear();
        refreshButtonClick(new ActionEvent());

    }
}
