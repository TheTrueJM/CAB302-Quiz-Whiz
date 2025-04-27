package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SignUpController {
    private final AuthController authController;

    @FXML private TextField usernameField;
    @FXML private TextField passwordField;
    @FXML private TextField confirmPasswordField;
    @FXML private Button signUpButton;

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
        return (Stage)signUpButton.getScene().getWindow();
    }

    @FXML
    protected void onFieldChanged(KeyEvent e) {
        TextField sender = (TextField)e.getSource();
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
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            if (!password.equals(confirmPassword)) {
                throw new IllegalArgumentException("Passwords do not match");
            }

            User newUser = authController.signUp(username, password);

            // Open Chat Page
            authController.authenticate(newUser, getStage());
        } catch (Exception e) {
            // TODO: Display possible Sign Up error messages to FXML
            System.err.println("User Sign Up Failed: " + e.getMessage() + e.getClass());
        }
    }

    // Open Login Page
    @FXML
    protected void switchToLogin() throws IOException, SQLException {
        authController.switchLayout("login", getStage());
    }
}
