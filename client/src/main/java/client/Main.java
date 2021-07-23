package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("./Mainform.fxml"));
        Parent root = loader.load();
        MainFormController controller = loader.getController();
        primaryStage.setTitle("Cloud storage");
        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setScene(scene);
        controller.setScene(scene);
        primaryStage.show();

        controller.authorisationWindowShow();
        primaryStage.setOnCloseRequest(controller.getCloseWindowEvent());

    }


    public static void main(String[] args) {
        launch(args);
    }
}

