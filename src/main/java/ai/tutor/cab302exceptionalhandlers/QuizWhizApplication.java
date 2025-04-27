package ai.tutor.cab302exceptionalhandlers;

import ai.tutor.cab302exceptionalhandlers.controller.AuthController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class QuizWhizApplication extends Application {
    public static final String TITLE = "Quiz Whiz";
    public static final int WIDTH = 640;
    public static final int HEIGHT = 580;

    @Override
    public void start(Stage stage) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                QuizWhizApplication.class.getResource("sign-up-view.fxml")
        );

        // In-Memory for developing
        SQLiteConnection db = new SQLiteConnection(true);
        AuthController controller = new AuthController(db);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);
        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
