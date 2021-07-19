package client;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import network.UserData;

public class AuthWindowController<T> {

    T ParentController;
    UserData collectedData;
    @FXML
    Button cancelButton;
    @FXML
    Button okButton;
    @FXML
    TextField tfLogin;
    @FXML
    TextField tfPassword;


    public void setParentController(T parentController) {
        ParentController = parentController;
    }

    public UserData getCollectedData() {
        return collectedData;
    }

    public void onOk(ActionEvent actionEvent) {
        collectedData = new UserData(tfLogin.getText().trim(), tfPassword.getText().trim());
        Stage stage = (Stage) okButton.getScene().getWindow();
        stage.close();
    }

    public void onCancel(ActionEvent actionEvent) {
        collectedData = null;
        Stage stage = (Stage) cancelButton.getScene().getWindow();
        stage.close();
    }
}
