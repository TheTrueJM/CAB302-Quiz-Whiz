package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserSettingsController {
    private UserDAO userDAO;
    private SQLiteConnection db;
    private User currentUser;
    private Stage stage;

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

    public UserSettingsController() throws SQLException {
        db = new SQLiteConnection();
        userDAO = new UserDAO(db);
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

                if(!AuthController.validUsername(usernameText)){
                    System.err.println("Invalid username...");
                    return;
                }

                currentUser.setUsername(usernameField.getText());

                // Only update if different
                if(!passwordText.equals(currentUser.getPasswordHash())){
                    if(!AuthController.validPassword(passwordText)){
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

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Utils.loadPage("login-view.fxml", LoginController.class, stage, new Object[]{db});
        });
    }

    private void setupLogoutButton(){
        logoutButton.setOnAction(actionEvent -> {
            currentUser = null;

            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Utils.loadPage("login-view.fxml", LoginController.class, stage, new Object[]{db});
        });
    }

    private void setupBackButton(){
        backButton.setOnAction(actionEvent -> {
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Utils.loadPage("chat-view.fxml", ChatController.class, stage, new Object[]{db, currentUser});
        });
    }
}
