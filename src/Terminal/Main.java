package Terminal;

import Backend.UserSession;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Stack;

public class Main extends Application {

    public static Stage primaryStage;
    public static Stack<Scene> scenes;
    public static UserSession session;

    @Override
    public void start(Stage primaryStage) throws Exception{
        scenes = new Stack<Scene>();
        this.primaryStage = primaryStage;
        setScene("login.fxml");
    }

    public static void main(String[] args) {
        session = new UserSession();
        launch(args);
    }

    public static void setScene(String fxml) {
        try {
            Parent root = FXMLLoader.load(Main.class.getResource(fxml));
            primaryStage.setTitle("Nexus Pay");
            Scene scene = new Scene(root, 640, 480);
            scenes.push(scene);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
