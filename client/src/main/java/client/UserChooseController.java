package client;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import objects.UserInfo;

import java.net.URL;
import java.util.ResourceBundle;

public class UserChooseController implements Initializable {
    private UserInfo selectedUser;
    private Stage stage;

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    ListView<UserInfo> lwUsers;
    @FXML
    Button btOk;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lwUsers.setCellFactory(param -> new ListCell<UserInfo>() {
            @Override
            protected void updateItem(UserInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isThisUserIsCurrent()) {
                    setText(null);
                } else {
                    setText(item.getUserName());
                }
            }
        });



    }

    public void onUsersClick(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount()==2){
            selectedUser = lwUsers.getSelectionModel().getSelectedItem();
            stage.close();
        }
    }

    public void onOK(ActionEvent actionEvent) {
        selectedUser = lwUsers.getSelectionModel().getSelectedItem();
        stage.close();
    }

    public void setUsersList(ObservableList<UserInfo> usersList) {
        lwUsers.setItems(usersList);
    }

    public UserInfo getSelected() {
        return selectedUser;
    }
}
