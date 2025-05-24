package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController extends AuthController {
    public LoginController(SQLiteConnection db) throws RuntimeException, SQLException {
        super(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    @Override
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
        }

        submitButtonToggle();
    }

    @Override
    protected void submitButtonToggle() {
        submitButton.setDisable(usernameEmpty || passwordEmpty);
    }

    @Override
    @FXML
    protected void onSubmit() throws IOException, SQLException {
        resetErrorFeedback();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User existingUser = authenticateUser(username, password);

            // Open Chat Page
            loadChat(existingUser);

        } catch (IllegalArgumentException e) {
            errorFeedback(usernameFeedback, e.getMessage());
        } catch (SecurityException e) {
            errorFeedback(passwordFeedback, e.getMessage());
        }
    }

    // Open Sign Up Page
    @Override
    @FXML
    protected void switchLayout() throws IOException, RuntimeException, SQLException {
        Utils.loadView("sign-up", new SignUpController(db), getStage());
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    @Override
    public User authenticateUser(String username, String password) throws IllegalArgumentException, SecurityException, SQLException {
        User existingUser = userDAO.getUser(username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Verify input password equals hashed user password
        if (!existingUser.verifyPassword(password)) {
            throw new SecurityException("Incorrect Password");
        }

        return existingUser;
    }
}
