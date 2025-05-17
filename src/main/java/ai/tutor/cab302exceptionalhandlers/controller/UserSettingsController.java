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
    @FXML private TextField passwordField;
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
        setupPasswordField();
    }

    /*
     * =========================================================================
     *                          FXML UI Controllers
     * =========================================================================
     */

    private void setupUsernameField(){
        usernameField.setText(currentUser.getUsername());
    }

    private void setupPasswordField(){
        passwordField.setText(currentUser.getPasswordHash());
    }

    private void setupSaveButton(){
        saveButton.setOnAction(actionEvent -> {
            try {
                String passwordText = passwordField.getText();
                String usernameText = usernameField.getText();

                if(!User.validUsername(usernameText)){
                    System.err.println("Invalid username...");
                    return;
                }

                currentUser.setUsername(usernameField.getText());

                // Only update if different
                if(!passwordText.equals(currentUser.getPasswordHash())){
                    if(!User.validPassword(passwordText)){
                        System.err.println("Invalid password...");
                        return;
                    }

                    String newPasswordHash = User.hashPassword(passwordText);
                    currentUser.setPasswordHash(newPasswordHash);
                }

                userDAO.updateUser(currentUser);
                System.out.println("User updated.");

            } catch (SQLException e) {
                e.printStackTrace();
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
}
