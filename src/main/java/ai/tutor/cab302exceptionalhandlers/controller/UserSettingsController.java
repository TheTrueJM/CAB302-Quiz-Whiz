package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.Optional;

public class UserSettingsController {
    private UserDAO userDAO;
    private SQLiteConnection db;
    private User currentUser;

    @FXML private Button saveButton;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    @FXML private Button deleteUserButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField studyArea;
    @FXML private ComboBox educationLevelCombo;
    @FXML private Label usernameFeedback;
    @FXML private Label passwordFeedback;
    @FXML private Label newPasswordFeedback;

    // User stats widgets
    @FXML private Label quizzesTakenLabel;
    @FXML private Label averageScoreLabel;

    private boolean usernameChanged;
    private boolean passwordChanged;

    public UserSettingsController(SQLiteConnection connection, User user) throws SQLException {
        db = connection;
        currentUser = user;
        userDAO = new UserDAO(db);
        this.usernameChanged = false;
        this.passwordChanged = false;
    }

    private Stage getStage() {
        return (Stage) saveButton.getScene().getWindow();
    }

    @FXML
    public void initialize(){
        setupBackButton();
        setupDeleteButton();
        setupLogoutButton();
        setupSaveButton();
        setupUsernameField();
    }

    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

    private void setupUsernameField(){
        usernameField.setText(currentUser.getUsername());
    }


    private void setupSaveButton(){
        saveButton.setOnAction(actionEvent -> {
            usernameFeedback.setText(null);
            passwordFeedback.setText(null);
            newPasswordFeedback.setText(null);

            String currentPasswordText = passwordField.getText();
            String newPasswordText = newPasswordField.getText();
            String usernameText = usernameField.getText();

            usernameChanged = false;
            passwordChanged = false;

            handleUsernameInput(usernameText);
            handlePasswordInput(newPasswordText, currentPasswordText);

            String successMessage = null;

            if (usernameChanged && passwordChanged) {
                successMessage = "Username & Password updated";
            } else if (usernameChanged) {
                successMessage = "Username updated";
            } else if (passwordChanged) {
                successMessage = "Password updated";
            }

            if (successMessage != null) {
                Utils.showInfoAlert(successMessage);
                passwordField.setText(null);
                newPasswordField.setText(null);
            }
        });
    }

    private void setupDeleteButton(){
        deleteUserButton.setOnAction(actionEvent -> {
            try {
                Optional<ButtonType> result = Utils.showConfirmAlert("Are you sure you want to logout of your account?");
                if (result.isPresent()) {
                    ButtonType buttonClicked = result.get();
                    if (buttonClicked == ButtonType.OK) {
                        userDAO.deleteUser(currentUser);
                    }
                }
            } catch (SQLException e) {
                Utils.showErrorAlert("Failed to logout");
            }

            try {
                Utils.loadView("login", new LoginController(db), getStage());
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Logging Out");
            }
        });
    }

    private void setupLogoutButton(){
        logoutButton.setOnAction(actionEvent -> {
            Optional<ButtonType> result = Utils.showConfirmAlert("Are you sure you want to logout?");
            if (result.isPresent()) {
                ButtonType buttonClicked = result.get();
                if (buttonClicked == ButtonType.OK) {
                    try {
                        Utils.loadView("login", new LoginController(db), getStage());
                    } catch (Exception e ) {
                        Utils.showErrorAlert("Error Logging Out");
                    }
                }
            }
        });
    }

    private void setupBackButton(){
        backButton.setOnAction(actionEvent -> {
            try {
                Utils.loadView("chat", new ChatController(db, currentUser), getStage());
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Returning To Chat");
            }
        });
    }

    private void handlePasswordInput(String newPassword, String currentPasswordText) {
        try {
            updatePassword(newPassword, currentPasswordText);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("null or empty")) {
                newPasswordFeedback.setText("Enter new password");
            } else if (e.getMessage().contains("invalid")) {
                passwordFeedback.setText("Invalid password, try again");
            }
        } catch (SecurityException e) {
            passwordFeedback.setText("Incorrect password");
        }
        catch (SQLException e) {
            Utils.showErrorAlert("Failed to change password");
        }
    }

    private void handleUsernameInput(String username) {
        try {
            updateUsername(username);
        } catch (IllegalArgumentException e) {
            if (e.getMessage().contains("already taken")) {
                usernameFeedback.setText("Username already taken");
            } else if (e.getMessage().contains("invalid")) {
                usernameFeedback.setText("Invalid username, try again");
            }
        } catch (SQLException e) {
            Utils.showErrorAlert("Failed to change username");
        }
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    public void updateUsername(String newUsername) throws IllegalArgumentException, SQLException {
        if (!User.validUsername(newUsername)){
            throw new IllegalArgumentException("Username is invalid");
        }

        if (!currentUser.getUsername().equals(newUsername)) {
            User existingUser = userDAO.getUser(newUsername);
            if (existingUser != null) {
                throw new IllegalArgumentException("Username is already taken");
            }
            currentUser.setUsername(newUsername);
            userDAO.updateUser(currentUser);
            usernameChanged = true;
        }
    }

    public void updatePassword(String newPassword, String currentPassword) throws IllegalArgumentException, SecurityException, SQLException {
        if (!Utils.validateNullOrEmpty(newPassword) || !Utils.validateNullOrEmpty(currentPassword)) {
            if (!Utils.validateNullOrEmpty(currentPassword) && Utils.validateNullOrEmpty(newPassword) ) {
                throw new IllegalArgumentException("New password is null or empty");
            }

            if (!User.validPassword(currentPassword)) {
                throw new IllegalArgumentException("Password is invalid");
            }

            if (!currentUser.verifyPassword(currentPassword)){
                throw new SecurityException("Incorrect Password");
            }
            String newPasswordHash = User.hashPassword(newPassword);
            currentUser.setPasswordHash(newPasswordHash);
            userDAO.updateUser(currentUser);
            passwordChanged = true;
        }
    }
}
