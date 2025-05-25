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

/**
 * Manages scene navigation and controller instantiation for the application.
 * <p>
 * Scene Manager is a singleton providing a centralized point
 * for switching between different views in the JavaFX application.
 * It uses a {@link ControllerFactory} to create controllers for each view.
 *
 * <h1>Usage Example:</h1>
 * <pre>
 * SceneManager.getInstance().initialize(primaryStage, controllerFactory);
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
    private boolean startUp = true;

    private SceneManager() {}

    /**
     * Gets the singleton instance of {@code SceneManager}.
     *
     * @return The singleton instance.
     */
    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    /**
     * Initializes the SceneManager with the primary stage and controller factory.
     * This method must be called only once at application startup.
     *
     * @param stage The primary {@link Stage} of the application.
     * @param factory The {@link ControllerFactory} used to create controllers.
     */
    public void initialize(Stage stage, ControllerFactory factory) {
        this.stage = stage;
        this.controllerFactory = factory;
    }

    /**
     * Navigates to the authentication view (Login or Sign Up).
     *
     * @param type The {@link AuthType} specifying whether to show the Login or Sign Up view.
     */
    public void navigateToAuth(AuthType type) {
        try {
            AuthController controller = controllerFactory.authController()
                .type(type)
                .build();

            String viewName = type.equals(AuthType.LOGIN) ? "login-view.fxml" : "sign-up-view.fxml";
            loadView(viewName, controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load auth view: " + e.getMessage());
        }
    }

    /**
     * Navigates to the main chat view.
     *
     * @param user The currently authenticated {@link User}.
     */
    public void navigateToChat(User user) {
        try {
            ChatController controller = controllerFactory.chatController()
                .currentUser(user)
                .build();

            loadView("chat-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load chat: " + e.getMessage());
        }
    }

    /**
     * Navigates to the chat setup view for creating or updating a chat.
     *
     * @param user The currently authenticated {@link User}.
     * @param type The {@link ChatSetupType} (CREATE or UPDATE).
     * @param chat The {@link Chat} to be updated, or null if creating a new chat.
     */
    public void navigateToChatSetup(User user, ChatSetupType type, Chat chat) {
        try {
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
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load chat setup: " + e.getMessage());
        }
    }

    /**
     * Navigates to the user settings view.
     *
     * @param user The currently authenticated {@link User}.
     */
    public void navigateToUserSettings(User user) {
        try {
            UserSettingsController controller = controllerFactory.userSettingsController()
                .currentUser(user)
                .build();

            loadView("user-settings-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load user settings: " + e.getMessage());
        }
    }

    /**
     * Navigates to the quiz view.
     *
     * @param quiz The {@link Quiz} to be displayed.
     * @param user The currently authenticated {@link User}.
     */
    public void navigateToQuiz(Quiz quiz, User user) {
        try {
            QuizController controller = controllerFactory.quizController()
                .quiz(quiz)
                .currentUser(user)
                .build();

            loadView("quiz-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load quiz: " + e.getMessage());
        }
    }

    /**
     * Loads an FXML file, sets its controller, and displays it on the main stage.
     * <p>
     * This method sets the default window width and height only once at startup. But
     * stays the same size after first run.
     *
     * @param fxmlFile The name of the FXML file to load (e.g., "login-view.fxml").
     * @param controller The controller instance for the view.
     */
    private void loadView(String fxmlFile, Object controller) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlFile));
            fxmlLoader.setController(controller);
            Scene scene = new Scene(fxmlLoader.load());

            stage.setScene(scene);

            /* This basically sets the default height and width only once at startup */
            if (startUp) {
                stage.setWidth(QuizWhizApplication.WIDTH);
                stage.setHeight(QuizWhizApplication.HEIGHT);
                startUp = false;
            }
        } catch (IOException e) {
            Utils.showErrorAlert("Failed to load view: " + e.getMessage());
        }
    }
}
