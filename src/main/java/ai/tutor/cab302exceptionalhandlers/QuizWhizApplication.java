package ai.tutor.cab302exceptionalhandlers;

import ai.tutor.cab302exceptionalhandlers.controller.*;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.application.Application;
import javafx.stage.Stage;

public class QuizWhizApplication extends Application {
    public static final String TITLE = "Quiz Whiz";
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    @Override
    public void start(Stage stage) {
        try {
            // In-Memory for developing
            SQLiteConnection db = new SQLiteConnection();
            ControllerFactory controllerFactory = new ControllerFactory(db);
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.initialize(stage, controllerFactory);

            stage.setTitle(TITLE);
            sceneManager.navigateToAuth("signup");
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
