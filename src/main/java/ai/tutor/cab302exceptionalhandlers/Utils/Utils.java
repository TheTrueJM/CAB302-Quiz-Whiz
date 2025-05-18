package ai.tutor.cab302exceptionalhandlers.Utils;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.Optional;

public final class Utils {
    public static boolean validateNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }


    // Generic function that loads fxml page and controller class
    public static void loadView(String viewName, Object controller, Stage stage) throws IOException, NullPointerException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                QuizWhizApplication.class.getResource( viewName + "-view.fxml")
        );
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
        stage.setScene(scene);
    }


    private static Optional<ButtonType> showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Alert");
        alert.setHeaderText(message);
        alert.setContentText(null);
        return alert.showAndWait();
    }

    public static void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, message);
    }

    public static void showInfoAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

    public static void showWarningAlert(String message) {
        showAlert(Alert.AlertType.WARNING, message);
    }

    public static Optional<ButtonType> showConfirmAlert(String message) {
        return showAlert(Alert.AlertType.CONFIRMATION, message);
    }
}
