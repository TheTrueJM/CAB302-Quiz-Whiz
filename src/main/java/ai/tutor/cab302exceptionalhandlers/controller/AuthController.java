package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.SceneManager;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public abstract class AuthController {
    protected final SQLiteConnection db;
    protected final UserDAO userDAO;

    protected boolean usernameEmpty = true;
    protected boolean passwordEmpty = true;

    @FXML
    protected TextField usernameField;
    @FXML
    protected PasswordField passwordField;
    @FXML
    protected PasswordField confirmPasswordField;

    @FXML
    protected Button submitButton;

    @FXML
    protected Label usernameFeedback;
    @FXML
    protected Label passwordFeedback;


    @FXML protected Button switchLayout;

    public AuthController(SQLiteConnection db) throws RuntimeException, SQLException {
        this.db = db;
        this.userDAO = new UserDAO(db);
    }

    protected Stage getStage() {
        return (Stage) submitButton.getScene().getWindow();
    }


    /*
     * =======================
     *    FXML UI Functions
     * =======================
     */

    @FXML
    public void initialize() {
        setupSwitchLayoutButton();
        setupSubmitButton();
        setupInputField();
    }

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
        SceneManager.getInstance().navigateToChat(user);
    }

    @FXML
    protected void switchLayout() throws IOException, RuntimeException, SQLException {
        AuthType targetType = this instanceof LoginController ? AuthType.SIGNUP : AuthType.LOGIN;
        SceneManager.getInstance().navigateToAuth(targetType);
    }

    @FXML
    // Set up event handler for switch to login/signup button
    protected void setupSwitchLayoutButton() {
        if (switchLayout != null) {
            switchLayout.setOnAction(event -> {
                try {
                    switchLayout();
                } catch (IOException | SQLException e) {
                    Utils.showErrorAlert("Failed to switch pages" + e.getMessage());
                }
            });
        }
    }

    @FXML
    // Set up event handler for submit button
    protected void setupSubmitButton() {
        if (submitButton != null) {
            submitButton.setOnAction(event -> {
                try {
                    onSubmit();
                } catch (IOException | SQLException e) {
                    Utils.showErrorAlert("Failed to submit: " + e.getMessage());
                }
            });
        }
    }

    @FXML
    protected  void setupInputField() {
        if (usernameField != null) {
            usernameField.setOnKeyReleased(this::onFieldChanged);
        }
        if (passwordField != null) {
            passwordField.setOnKeyReleased(this::onFieldChanged);
        }
    }

    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    public abstract User authenticateUser(String username, String password) throws IllegalArgumentException, SecurityException, SQLException;
}
