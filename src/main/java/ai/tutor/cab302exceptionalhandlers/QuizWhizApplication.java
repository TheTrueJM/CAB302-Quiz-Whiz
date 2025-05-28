package ai.tutor.cab302exceptionalhandlers;

import ai.tutor.cab302exceptionalhandlers.factories.ControllerFactory;
import ai.tutor.cab302exceptionalhandlers.model.*;
import ai.tutor.cab302exceptionalhandlers.types.AuthType;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main application class for Quiz Whiz.
 * <p>
 * By default, the resolution for the application is set to 1280x720.
 *
 * @author Joshua M.
 */
public class QuizWhizApplication extends Application {
    public static final String TITLE = "Quiz Whiz";
    public static final int WIDTH = 1280;
    public static final int HEIGHT = 720;

    /**
     * Initializes the main application loop, called by the JavaFX framework when the application starts.
     * <p>
     * A {@link SceneManager} instance is retrieved and given
     * the application stage to manage the application scenes.
     * <p>
     * The first page navigated to at startup is the Sign Up authentication page.
     *
     * @param stage The primary stage for this application, onto which the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        try {
            stage.setTitle(TITLE);
            stage.setWidth(WIDTH);
            stage.setHeight(HEIGHT);

            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.applicationInitialize(stage);
            sceneManager.navigateToAuth(AuthType.SIGNUP);

            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to start application: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
