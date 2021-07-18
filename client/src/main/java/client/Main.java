package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import objects.UserData;

import java.util.Scanner;

@Slf4j
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("./Mainform.fxml"));
        Parent root = loader.load();// loader.load(getClass().getResource("./Mainform.fxml"));
        MainFormController controller = loader.getController();
        primaryStage.setTitle("Cloud storage");
        primaryStage.setScene(new Scene(root, 1200, 700));
        primaryStage.show();
        primaryStage.setOnCloseRequest(controller.getCloseWindowEvent());

    }


    public static void main(String[] args) {
        launch(args);
    }
}

