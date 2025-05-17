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
    @FXML private PasswordField passwordField;
    @FXML private PasswordField newPasswordField;
    @FXML private TextField studyArea;
    @FXML private ComboBox educationLevelCombo;
    @FXML private Label usernameFeedback;
    @FXML private Label passwordFeedback;

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


    private void setupSaveButton(){
        saveButton.setOnAction(actionEvent -> {
            try {
                usernameFeedback.setText(null);
                passwordFeedback.setText(null);

                String passwordText = passwordField.getText();
                String newPasswordText = newPasswordField.getText();
                String usernameText = usernameField.getText();

                String originalUsername = currentUser.getUsername();

                Boolean usernameChanged = false;
                Boolean passwordChanged = false;

                if (!User.validUsername(usernameText)){
                    usernameFeedback.setText("Invalid username, try again");
                    return;
                }

                if (!originalUsername.equals(usernameField.getText())) {
                    for (User user : userDAO.getAllUsers()) {
                        if (user.getUsername().equals(usernameText)) {
                            usernameFeedback.setText("Username already exists");
                            return;
                        }
                    }
                    currentUser.setUsername(usernameField.getText());
                    usernameChanged = true;
                }


                if (Utils.validateNullOrEmpty(newPasswordText)) {
                    if (!User.validPassword(passwordText)) {
                        passwordFeedback.setText("Invalid password, try again");
                        return;
                    }

                    if (!currentUser.verifyPassword(passwordText)){
                        passwordFeedback.setText("Wrong Password");
                        return;
                    }

                    String newPasswordHash = User.hashPassword(newPasswordText);
                    currentUser.setPasswordHash(newPasswordHash);
                    passwordChanged = true;
                }

                String successMessage = null;

                if (usernameChanged && passwordChanged) {
                    successMessage = "Username & Password updated";
                } else if (usernameChanged) {
                    successMessage = "Username updated";
                } else if (passwordChanged) {
                    successMessage = "Password updated";
                }

                if (successMessage != null) {
                    userDAO.updateUser(currentUser);
                    Utils.showAlert(Alert.AlertType.INFORMATION, "Success", successMessage);
                    passwordField.setText(null);
                    newPasswordField.setText(null);
                }

            } catch (SQLException e) {
                    Utils.showAlert(Alert.AlertType.INFORMATION, "Failed to update details: ", e.getMessage());
            }
        });
    }

    private void setupDeleteButton(){
        deleteUserButton.setOnAction(actionEvent -> {
            try {
                Optional<ButtonType> result = Utils.showAlert(Alert.AlertType.CONFIRMATION, "Logout", "Are you sure you want to logout of your account?");
                if (result.isPresent()) {
                    ButtonType buttonClicked = result.get();
                    if (buttonClicked == ButtonType.OK) {
                        userDAO.deleteUser(currentUser);
                    }
                }
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
            Optional<ButtonType> result = Utils.showAlert(Alert.AlertType.CONFIRMATION, "Logout", "Are you sure you want to logout of your account?");
            if (result.isPresent()) {
                ButtonType buttonClicked = result.get();
                if (buttonClicked == ButtonType.OK) {
                    try {
                        Utils.loadView("login", new LoginController(db), getStage());
                    } catch (Exception e ) {
                        Utils.showErrorAlert("Error Logging Out: " + e);
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
