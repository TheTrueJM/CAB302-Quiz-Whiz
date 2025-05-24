package ai.tutor.cab302exceptionalhandlers.Utils;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

/**
 * General utility class for helper methods
 *
 * @author Mason
 */
public final class Utils {
    private Utils() {
    }

    /**
     * Validates if a string is null or empty (after trimming whitespace).
     *
     * @param value The string to validate.
     * @return True if the string is null or empty, false otherwise.
     */
    public static boolean validateNullOrEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    /**
     * Displays alert dialog containing the alert message
     *
     * @param type    The type of alert to display
     * @param message The message to display in the alert dialog
     * @return An {@link Optional} containing the {@link ButtonType} clicked by the user, or empty.
     */
    private static Optional<ButtonType> showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Alert");
        alert.setHeaderText(message);
        alert.setContentText(null);
        return alert.showAndWait();
    }

    /**
     * Displays an error alert dialog with the specified message.
     *
     * @param message The error message to display.
     */
    public static void showErrorAlert(String message) {
        showAlert(Alert.AlertType.ERROR, message);
    }

    /**
     * Displays an information alert dialog with the specified message.
     *
     * @param message The informational message to display.
     */
    public static void showInfoAlert(String message) {
        showAlert(Alert.AlertType.INFORMATION, message);
    }

    /**
     * Displays a warning alert dialog with the specified message.
     *
     * @param message The warning message to display.
     */
    public static void showWarningAlert(String message) {
        showAlert(Alert.AlertType.WARNING, message);
    }

    /**
     * Displays a confirmation alert dialog with the specified message and returns the user's choice.
     *
     * @param message The confirmation message to display.
     * @return An {@link Optional} containing the {@link ButtonType} clicked by the user (e.g., OK, Cancel).
     */
    public static Optional<ButtonType> showConfirmAlert(String message) {
        return showAlert(Alert.AlertType.CONFIRMATION, message);
    }
}
