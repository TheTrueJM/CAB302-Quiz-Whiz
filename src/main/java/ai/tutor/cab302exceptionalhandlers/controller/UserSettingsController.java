package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class UserSettingsController {
    private UserDAO userDAO;
    private SQLiteConnection db;
    private User currentUser;

    @FXML private Button saveButton;
    @FXML private Button logoutButton;
    @FXML private Button backButton;
    @FXML private Button deleteUserButton;
    @FXML private TextField usernameField;
    @FXML private TextField currentPasswordField;
    @FXML private TextField newPasswordField;
    @FXML private TextField confirmPasswordField;
    @FXML private TextField studyArea;
    @FXML private ComboBox educationLevelCombo;

    // User stats widgets
    @FXML private Label quizzesTakenLabel;
    @FXML private Label averageScoreLabel;

    public UserSettingsController(SQLiteConnection connection, User user) throws SQLException {
        db = connection;
        currentUser = user;
        userDAO = new UserDAO(db);
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


    // TODO: Add separate buttons for change username and password
    private void setupSaveButton(){
        saveButton.setOnAction(actionEvent -> {
            try {
                String username = usernameField.getText();
                String currentPassword = currentPasswordField.getText();
                String newPassword = newPasswordField.getText();
                String confirmPassword = confirmPasswordField.getText();

                if (!newPassword.equals(confirmPassword)) {
                    throw new SecurityException("Passwords do not match");
                }

                if (Utils.validateNullOrEmpty(currentPassword) && Utils.validateNullOrEmpty(newPassword)) {
                    updateUsername(username);
                } else {
                    updatePassword(newPassword, currentPassword);
                }
                Utils.showInfoAlert("User details updated");
            } catch (Exception e) {
                Utils.showErrorAlert(e.getMessage());
            }
        });
    }

    private void setupDeleteButton(){
        deleteUserButton.setOnAction(actionEvent -> {
            try {

                //TODO: Should probably make a popup asking user to confirm
                userDAO.deleteUser(currentUser);

            } catch (SQLException e) {
                e.printStackTrace();
            }

            try {
                Utils.loadView("login", new LoginController(db), getStage());
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Logging Out: " + e);
            }
        });
    }

    private void setupLogoutButton(){
        logoutButton.setOnAction(actionEvent -> {
            try {
                Utils.loadView("login", new LoginController(db), getStage());
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Logging Out: " + e);
            }
        });
    }

    private void setupBackButton(){
        backButton.setOnAction(actionEvent -> {
            try {
                Utils.loadView("chat", new ChatController(db, currentUser), getStage());
            } catch (Exception e ) {
                Utils.showErrorAlert("Error Returning To Chat: " + e);
            }
        });
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    public void updateUsername(String newUsername) throws IllegalArgumentException, SQLException {
        if (!User.validUsername(newUsername)) {
            throw new IllegalArgumentException("Username is invalid");
        }

        if (!newUsername.equals(currentUser.getUsername())) {
            User existingUser = userDAO.getUser(newUsername);
            if (existingUser != null) {
                throw new IllegalArgumentException("Username is already taken");
            }
        }

        currentUser.setUsername(newUsername);
        userDAO.updateUser(currentUser);
    }

    public void updatePassword(String newPassword, String currentPassword) throws IllegalArgumentException, SecurityException, SQLException {
        if (!User.validPassword(newPassword)) {
            throw new IllegalArgumentException("Password is invalid");
        }
        if (!currentUser.verifyPassword(currentPassword != null ? currentPassword : "")) {
            throw new SecurityException("Incorrect Password");
        }

        String hashedNewPassword = User.hashPassword(newPassword);
        currentUser.setPasswordHash(hashedNewPassword);
        userDAO.updateUser(currentUser);
    }
}
