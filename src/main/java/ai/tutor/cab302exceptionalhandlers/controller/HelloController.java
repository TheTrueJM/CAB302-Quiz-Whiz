package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class HelloController {
    @FXML
    private Label welcomeText;

    @FXML
    private Button nextButton;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    @FXML
    protected void onNextButtonClick() throws IOException {
        try {
            Stage stage = (Stage) nextButton.getScene().getWindow();
            FXMLLoader fxmlLoader = new FXMLLoader(QuizWhizApplication.class.getResource("sign-up-view.fxml"));

            SQLiteConnection db = new SQLiteConnection();
            AuthController controller = new AuthController(db);
            fxmlLoader.setController(controller);

            Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
            stage.setScene(scene);
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }
}
