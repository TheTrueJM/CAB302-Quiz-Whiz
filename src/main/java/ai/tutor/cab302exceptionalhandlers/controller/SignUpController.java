package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SignUpController extends AuthController {
    private boolean passwordCEmpty = true;

    @FXML
    private TextField confirmPasswordField;


    public SignUpController(SQLiteConnection db) throws RuntimeException, SQLException {
        super(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    @Override
    public void initialize() {
        super.initialize();
        setupConfirmPasswordField();
    }

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
            case "confirmPasswordField":
                passwordCEmpty = senderText.isEmpty();
                break;
        }

        submitButtonToggle();
    }

    @FXML
    protected  void setupConfirmPasswordField() {
        if (confirmPasswordField != null) {
            confirmPasswordField.setOnKeyReleased(this::onFieldChanged);
        }
    }

    @Override
    protected void submitButtonToggle() {
        submitButton.setDisable(usernameEmpty || passwordEmpty || passwordCEmpty);
    }

    @Override
    @FXML
    protected void onSubmit() throws IOException, RuntimeException, SQLException {
        resetErrorFeedback();
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        try {
            if (!password.equals(confirmPassword)) {
                throw new SecurityException("Passwords do not match");
            }

            User newUser = authenticateUser(username, password);

            // Open Chat Page
            loadChat(newUser);

        } catch (IllegalArgumentException e) {
            errorFeedback(usernameFeedback, e.getMessage());
        } catch (SecurityException e) {
            errorFeedback(passwordFeedback, e.getMessage());
        }
    }

    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    @Override
    public User authenticateUser(String username, String password) throws IllegalArgumentException, SQLException {
        String hashedPassword = User.hashPassword(password);
        User newUser = new User(username, hashedPassword);

        User existingUser = userDAO.getUser(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username is already taken");
        }

        userDAO.createUser(newUser);
        return newUser;
    }
}
