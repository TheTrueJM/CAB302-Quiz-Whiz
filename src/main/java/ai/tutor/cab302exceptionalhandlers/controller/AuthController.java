package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import ai.tutor.cab302exceptionalhandlers.model.*;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class AuthController {
    private final SQLiteConnection db;
    private final UserDAO userDAO;


    public AuthController(SQLiteConnection db) throws RuntimeException, SQLException {
        this.db = db;
        this.userDAO = new UserDAO(db);
    }


    /*
     * =========================
     *    FXML UI Controllers
     * =========================
     */

    public void authenticate(User user, Stage stage) throws IOException, SQLException {
        FXMLLoader fxmlLoader = new FXMLLoader(
                QuizWhizApplication.class.getResource("chat-view.fxml")
        );

        ChatController controller = new ChatController(db, user);
        fxmlLoader.setController(controller);

        Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
        stage.setScene(scene);
    }

    public void switchLayout(String layout, Stage stage) throws IOException, SQLException {
        if (layout.equals("sign-up") || layout.equals("login")) {
            FXMLLoader fxmlLoader = new FXMLLoader(
                    QuizWhizApplication.class.getResource(layout + "-view.fxml")
            );

            if (layout.equals("sign-up")) {
                SignUpController controller = new SignUpController(db);
                fxmlLoader.setController(controller);
            } else {
                LoginController controller = new LoginController(db);
                fxmlLoader.setController(controller);
            }

            Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
            stage.setScene(scene);
        }
    }


    /*
     * =====================
     *    CRUD Operations
     * =====================
     */

    public User signUp(String username, String password) throws IllegalArgumentException, SecurityException, SQLException {
        if (!validUsername(username)) {
            throw new IllegalArgumentException("Username is invalid");
        }
        if (!validPassword(password)) {
            throw new SecurityException ("Password is invalid");
        }

        User existingUser = userDAO.getUser(username);
        if (existingUser != null) {
            throw new IllegalArgumentException("Username is already taken");
        }

        String hashedPassword = User.hashPassword(password);
        User newUser = new User(username, hashedPassword);
        userDAO.createUser(newUser);

        return newUser;
    }

    public User login(String username, String password) throws IllegalArgumentException, SecurityException, SQLException {
        User existingUser = userDAO.getUser(username == null ? "" : username);
        if (existingUser == null) {
            throw new IllegalArgumentException("User does not exist");
        }

        // Verify input password equals hashed user password
        if (!existingUser.verifyPassword(password == null ? "" : password)) {
            throw new SecurityException("Incorrect Password");
        }

        return existingUser;
    }

    public static boolean validUsername(String username) {
        return username != null && username.matches("^[a-zA-Z0-9]+$");
    }

    public static boolean validPassword(String password) {
        return password != null && password.matches("^[a-zA-Z0-9]+$");
    }

    public static void feedbackError(Label feedbackNode, String message) {
        feedbackNode.setText(message);
    }

    public static void resetFeedbackError(Label feedbackNode, Label feedbackNode2) {
        feedbackNode.setText("");
    }
}
