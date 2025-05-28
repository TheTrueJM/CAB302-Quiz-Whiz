package ai.tutor.cab302exceptionalhandlers;

import ai.tutor.cab302exceptionalhandlers.controller.*;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.factories.ControllerFactory;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import ai.tutor.cab302exceptionalhandlers.types.ChatSetupType;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Manages scene navigation and controller instantiation for the application.
 * <p>
 * Scene Manager is a singleton providing a centralized point
 * for switching between different views in the JavaFX application.
 * It uses a {@link ControllerFactory} to create controllers for each view.
 *
 * <p>Usage Example:
 * <pre>
 * SceneManager.getInstance().applicationInitialize(primaryStage);
 * SceneManager.getInstance().navigateToAuth(AuthType.LOGIN);
 * </pre>
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.factories.ControllerFactory
 */
public class SceneManager {
    private static SceneManager instance;
    private Stage stage;
    private ControllerFactory controllerFactory;


    /**
     * Initializes the scene manager for the application.
     * <p>
     * A new instance of {@link SQLiteConnection} is created for database access, and given
     * to {@link ControllerFactory} to manage application controller initialization.
     *
     * @throws SQLException if a database connection error occurs.
     */
    private SceneManager() throws SQLException {
        controllerFactory = new ControllerFactory(new SQLiteConnection());
    }

    /**
     * Gets the singleton instance of {@code SceneManager}.
     *
     * @return The singleton instance.
     * @throws SQLException if a database connection error occurs.
     */
    public static SceneManager getInstance() throws SQLException {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Initializes the SceneManager with the primary stage.
     * This method must be called once before application startup.
     *
     * @param stage The primary {@link Stage} of the application.
     */
    public void applicationInitialize(Stage stage) {
        this.stage = stage;
    }

    /**
     * Navigates to the authentication view (Login or Sign Up).
     *
     * @param type The {@link AuthType} specifying whether to show the Login or Sign Up view.
     * @throws IllegalStateException if the authentication type is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     * @throws IOException if the authentication view cannot be loaded.
     */
    public void navigateToAuth(AuthType type) throws IllegalStateException, RuntimeException, SQLException, IOException {
        AuthController controller = controllerFactory.authController()
            .type(type)
            .build();

        String viewName = type.equals(AuthType.LOGIN) ? "login-view.fxml" : "sign-up-view.fxml";
        loadView(viewName, controller);
    }

    /**
     * Navigates to the main chat view.
     *
     * @param user The currently authenticated {@link User}.
     * @throws IllegalStateException if the user is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     * @throws IOException if the chat view cannot be loaded.
     */
    public void navigateToChat(User user) throws IllegalStateException, RuntimeException, SQLException, IOException {
        ChatController controller = controllerFactory.chatController()
            .currentUser(user)
            .build();

        loadView("chat-view.fxml", controller);
    }

    /**
     * Navigates to the chat setup view for creating or updating a chat.
     *
     * @param user The currently authenticated {@link User}.
     * @param type The {@link ChatSetupType} (CREATE or UPDATE).
     * @param chat The {@link Chat} to be updated, or null if creating a new chat.
     * @throws IllegalStateException if the user or chat (for update) is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     * @throws IOException if the chat setup view cannot be loaded.
     */
    public void navigateToChatSetup(User user, ChatSetupType type, Chat chat) throws IllegalStateException, RuntimeException, SQLException, IOException {
        ChatSetupController controller;
        if (chat == null) {
            controller = controllerFactory.chatSetupController()
                .currentUser(user)
                .type(type)
                .build();
        } else {
            controller = controllerFactory.chatSetupController()
                .currentUser(user)
                .type(type)
                .currentChat(chat)
                .build();
        }

        loadView("chat-setup-view.fxml", controller);
    }

    /**
     * Navigates to the user settings view.
     *
     * @param user The currently authenticated {@link User}.
     * @throws IllegalStateException if the user is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     * @throws IOException if the user settings view cannot be loaded.
     */
    public void navigateToUserSettings(User user) throws IllegalStateException, RuntimeException, SQLException, IOException {
        UserSettingsController controller = controllerFactory.userSettingsController()
            .currentUser(user)
            .build();

        loadView("user-settings-view.fxml", controller);
    }

    /**
     * Navigates to the quiz view.
     *
     * @param quiz The {@link Quiz} to be displayed.
     * @param user The currently authenticated {@link User}.
     * @throws IllegalStateException if the user or quiz is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     * @throws IOException if the quiz view cannot be loaded.
     */
    public void navigateToQuiz(Quiz quiz, User user) throws IllegalStateException, RuntimeException, SQLException, IOException {
        QuizController controller = controllerFactory.quizController()
            .quiz(quiz)
            .currentUser(user)
            .build();

        loadView("quiz-view.fxml", controller);
    }


    /**
     * Loads an FXML file, sets its controller, and displays this scene on the main stage.
     *
     * @param fxmlFile The name of the FXML file to load (e.g., "login-view.fxml").
     * @param controller The controller instance for the view.
     * @throws IllegalStateException if the application stage was not initialized.
     * @throws IOException if the scene view cannot be loaded.
     */
    private void loadView(String fxmlFile, Object controller) throws IllegalStateException, IOException {
        if (stage == null) {
            throw new IllegalStateException("The application was not initialized with a stage");
        }

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
        fxmlLoader.setController(controller);
        Scene scene = new Scene(fxmlLoader.load());
        stage.setScene(scene);
    }
}
