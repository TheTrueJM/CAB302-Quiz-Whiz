package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import io.github.ollama4j.models.request.Auth;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SignUpController {
    private final AuthController authController;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private TextField confirmPasswordField;
    @FXML
    private Button signUpButton;
    @FXML
    private Label usernameFeedback;
    @FXML
    private Label passwordFeedback;

    private boolean usernameEmpty = true;
    private boolean passwordEmpty = true;
    private boolean passwordCEmpty = true;


    public SignUpController(SQLiteConnection db) throws RuntimeException, SQLException {
        this.authController = new AuthController(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    private Stage getStage() {
        return (Stage) signUpButton.getScene().getWindow();
    }

    @FXML
    protected void onFieldChanged(KeyEvent e) {
        TextField sender = (TextField) e.getSource();
        String senderID = sender.getId();
        String senderText = sender.getText();

        switch (senderID) {
            case "usernameField":
                usernameEmpty = senderText.isEmpty();
                break;
            case "passwordField":
                passwordEmpty = senderText.isEmpty();
                break;
            case "confirmPasswordField":
                passwordCEmpty = senderText.isEmpty();
                break;
        }

        submitButtonToggle();
    }

    private void submitButtonToggle() {
        signUpButton.setDisable(usernameEmpty || passwordEmpty || passwordCEmpty);
    }

    @FXML
    protected void onSignUp() throws IOException, SQLException {
        AuthController.resetFeedbackError(usernameFeedback,passwordFeedback);
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            if (!password.equals(confirmPassword)) {
                AuthController.feedbackError(passwordFeedback, "Passwords do not match");
            }
            else {
                User newUser = authController.signUp(username, password);

                // Open Chat Page
                authController.authenticate(newUser, getStage());
            }

        } catch (IllegalArgumentException e) {
            AuthController.feedbackError(usernameFeedback, e.getMessage());
        } catch (SecurityException e) {
            AuthController.feedbackError(passwordFeedback, e.getMessage());
        }
    }

    // Open Login Page
    @FXML
    protected void switchToLogin() throws IOException, SQLException {
        authController.switchLayout("login", getStage());
    }
}
