package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AuthController {
    @FXML
    protected TextField usernameField;
    @FXML
    protected TextField passwordField;

    @FXML
    protected Button submitButton;

    @FXML
    protected Label usernameFeedback;
    @FXML
    protected Label passwordFeedback;

    protected boolean usernameEmpty = true;
    protected boolean passwordEmpty = true;

    protected final SQLiteConnection db;
    protected final UserDAO userDAO;


    public AuthController(SQLiteConnection db) throws RuntimeException, SQLException {
        this.db = db;
        this.userDAO = new UserDAO(db);
    }

    protected Stage getStage() {
        return (Stage) submitButton.getScene().getWindow();
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    @FXML
    protected abstract void onFieldChanged(KeyEvent e);

    protected abstract void submitButtonToggle();

    protected void errorFeedback(Label feedbackLabel, String message) {
        feedbackLabel.setText(message);
    }

    protected void resetErrorFeedback() {
        usernameFeedback.setText("");
        passwordFeedback.setText("");
    }

    @FXML
    protected abstract void onSubmit() throws IOException, SQLException;

    public void loadChat(User user) throws IOException, RuntimeException, SQLException {
        Utils.loadView("chat", new ChatController(db, user), getStage());
    }

    @FXML
    protected abstract void switchLayout() throws IOException, RuntimeException, SQLException;

    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    public abstract User authenticateUser(String username, String password) throws IllegalArgumentException, SecurityException, SQLException;
}
