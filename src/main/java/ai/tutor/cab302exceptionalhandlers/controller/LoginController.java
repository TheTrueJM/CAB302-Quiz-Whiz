package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import java.io.IOException;
import java.sql.SQLException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

public class LoginController {
    private final AuthController authController;

    @FXML
    private TextField usernameField;
    @FXML
    private TextField passwordField;
    @FXML
    private Button loginButton;

    private boolean usernameEmpty = true;
    private boolean passwordEmpty = true;

    public LoginController(SQLiteConnection db) throws RuntimeException, SQLException {
        this.authController = new AuthController(db);
    }

    /* FXML UI Controllers */

    private Stage getStage() {
        return (Stage) loginButton.getScene().getWindow();
    }

    @FXML
    protected void onFieldChanged(KeyEvent e) {
        TextField sender = (TextField) e.getSource();
        String senderID = sender.getId();
        String senderText = sender.getText();

        switch (senderID) {
            case "usernameField" :
                usernameEmpty = senderText.isEmpty();
                break;
            case "passwordField" :
                passwordEmpty = senderText.isEmpty();
                break;
        }

        submitButtonToggle();
    }

    private void submitButtonToggle() {
        loginButton.setDisable(usernameEmpty || passwordEmpty);
    }

    @FXML
    protected void onLogin() throws IOException, SQLException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User existingUser = authController.login(username, password);

            // Open Chat Page
            authController.authenticate(existingUser, getStage());
        } catch (Exception e) {
            // TODO: Display possible Login error messages to FXML
            System.err.println("User Login Failed: " + e.getMessage() + e.getClass());
        }
    }

    // Open Login Page
    @FXML
    protected void switchToSignUp() throws IOException, SQLException {
        authController.switchLayout("sign-up", getStage());
    }
}
