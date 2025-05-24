package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.SceneManager;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

public class UserSettingsController {
    private SQLiteConnection db;
    private UserDAO userDAO;
    private User currentUser;

    private boolean usernameChanged = false;
    private boolean passwordChanged = false;

    @FXML private Button backButton;
    @FXML private Button logoutButton;
    @FXML private Button saveButton;
    @FXML private Button deleteUserButton;

    @FXML private TextField usernameField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label usernameFeedback;
    @FXML private Label currentPasswordFeedback;
    @FXML private Label newPasswordFeedback;

    // User stats widgets
    @FXML private Label quizzesTakenLabel;
    @FXML private Label averageScoreLabel;

    public UserSettingsController(SQLiteConnection connection, User user) throws SQLException {
        db = connection;
        userDAO = new UserDAO(db);
        currentUser = user;
    }


    @FXML
    public void initialize() {
        setupUsernameField();
    }

    private void setupUsernameField() {
        usernameField.setText(currentUser.getUsername());
    }

    private Stage getStage() {
        return (Stage) saveButton.getScene().getWindow();
    }


    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

    @FXML
    private void onBack() throws IOException, RuntimeException, SQLException {
        SceneManager.getInstance().navigateToChat(currentUser);
    }

    @FXML
    private void onLogout() throws IOException, RuntimeException, SQLException {
        Optional<ButtonType> result = Utils.showConfirmAlert("Are you sure you want to logout?");
        if (result.isPresent()) {
            ButtonType buttonClicked = result.get();
            if (buttonClicked == ButtonType.OK) {
                SceneManager.getInstance().navigateToAuth(AuthType.LOGIN);
            }
        }
    }

    @FXML
    private void onSave() {
        usernameFeedback.setText("");
        currentPasswordFeedback.setText("");
        newPasswordFeedback.setText("");

        String username = usernameField.getText();
        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        usernameChanged = passwordChanged = false;

        handleUsernameUpdate(username);
        handlePasswordUpdate(currentPassword, newPassword, confirmPassword);

        String feedbackMessage;
        if (usernameChanged && passwordChanged) {
            feedbackMessage = "Username & Password updated";
        } else if (usernameChanged) {
            feedbackMessage = "Username updated";
        } else if (passwordChanged) {
            feedbackMessage = "Password updated";
        } else {
            feedbackMessage = "No details updated";
        }

        Utils.showInfoAlert(feedbackMessage);
        currentPasswordField.setText("");
        newPasswordField.setText("");
        confirmPasswordField.setText("");
    }

    @FXML
    private void onDelete() throws IOException, RuntimeException, SQLException {
        Optional<ButtonType> result = Utils.showConfirmAlert("Are you sure you want to delete of your account?");
        if (result.isPresent()) {
            ButtonType buttonClicked = result.get();
            if (buttonClicked == ButtonType.OK) {;
                try {
                    userDAO.deleteUser(currentUser);
                    SceneManager.getInstance().navigateToAuth(AuthType.SIGNUP);
                } catch (SQLException e) {
                    Utils.showErrorAlert("Failed to delete account: " + e.getMessage());
                }
            }
        }
    }

    private void handleUsernameUpdate(String username) {
        try {
            updateUsername(username);
            usernameChanged = true;
        } catch (Exception e) {
            usernameFeedback.setText(e.getMessage());
        }
    }

    private void handlePasswordUpdate(String currentPassword, String newPassword, String confirmPassword) {
        if (!currentPassword.isEmpty() || !newPassword.isEmpty() || !confirmPassword.isEmpty()) {
            try {
                if (!newPassword.equals(confirmPassword)) {
                    throw new IllegalArgumentException("Passwords do not match");
                }

                updatePassword(currentPassword, newPassword);
                passwordChanged = true;

            } catch (IllegalArgumentException e) {
                newPasswordFeedback.setText(e.getMessage());
            } catch (Exception e) {
                currentPasswordFeedback.setText(e.getMessage());
            }
        }
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    public void updateUsername(String newUsername) throws IllegalArgumentException, SQLException {
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

    public void updatePassword(String currentPassword, String newPassword) throws IllegalArgumentException, SecurityException, SQLException {
        if (newPassword == null || newPassword.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be empty");
        } else if (!currentUser.verifyPassword(currentPassword)) {
            throw new SecurityException("Incorrect Password");
        }

        String newPasswordHash = User.hashPassword(newPassword);
        currentUser.setPasswordHash(newPasswordHash);
        userDAO.updateUser(currentUser);
        passwordChanged = true;
    }
}
