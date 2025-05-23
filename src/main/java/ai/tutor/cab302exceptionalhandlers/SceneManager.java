package ai.tutor.cab302exceptionalhandlers;

import ai.tutor.cab302exceptionalhandlers.controller.*;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.Utils.Utils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class SceneManager {
    private static SceneManager instance;
    private Stage stage;
    private ControllerFactory controllerFactory;
    private boolean startUp = true;

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void initialize(Stage stage, ControllerFactory factory) {
        this.stage = stage;
        this.controllerFactory = factory;
    }

    public void navigateToAuth(String type) {
        try {
            AuthController controller = controllerFactory.createAuthController(type);
            String viewName = type.equals("login") ? "login-view.fxml" : "sign-up-view.fxml";
            loadView(viewName, controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load auth view: " + e.getMessage());
        }
    }

    public void navigateToChat(User user) {
        try {
            ChatController controller = controllerFactory.createChatController(user);
            loadView("chat-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load chat: " + e.getMessage());
        }
    }

    public void navigateToChatSetup(User user, String type, Chat chat) {
        try {
            ChatSetupController controller = controllerFactory.createChatSetupController(user, type, chat);
            loadView("chat-setup-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load chat setup: " + e.getMessage());
        }
    }

    public void navigateToUserSettings(User user) {
        try {
            UserSettingsController controller = controllerFactory.createUserSettingsController(user);
            loadView("user-settings-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load user settings: " + e.getMessage());
        }
    }

    public void navigateToQuiz(Quiz quiz, User user) {
        try {
            QuizController controller = controllerFactory.createQuizController(quiz, user);
            loadView("quiz-view.fxml", controller);
        } catch (Exception e) {
            Utils.showErrorAlert("Failed to load quiz: " + e.getMessage());
        }
    }

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
