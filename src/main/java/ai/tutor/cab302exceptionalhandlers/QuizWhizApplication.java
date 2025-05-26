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
     * A new instance of {@link SQLiteConnection} is created for database access, and the
     * application controllers are initialized. In addition, a {@link SceneManager} instance
     * is created to manage the application scenes.
     * <p>
     * The first page navigated to at startup is the Sign Up authentication page.
     *
     * @param stage The primary stage for this application, onto which the application scene can be set.
     */
    @Override
    public void start(Stage stage) {
        try {
            // In-Memory for developing
            SQLiteConnection db = new SQLiteConnection();
            ControllerFactory controllerFactory = new ControllerFactory(db);
            SceneManager sceneManager = SceneManager.getInstance();
            sceneManager.initialize(stage, controllerFactory);

            stage.setTitle(TITLE);
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
