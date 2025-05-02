package ai.tutor.cab302exceptionalhandlers.model;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.util.Arrays;

public final class Utils {
    // Generic function that loads fxml page and controller class
    // TODO: Move to accessible place
    public static <T> void loadPage(String fileName, Class<T> controllerClass, Stage stage, Object[] params){
        FXMLLoader fxmlLoader = new FXMLLoader(
                QuizWhizApplication.class.getResource(fileName)
        );

        try {
            // Get the first constructor
            Constructor<?> constructor = controllerClass.getDeclaredConstructors()[0];
            Class<?>[] paramTypes = constructor.getParameterTypes();

            // Validate parameter count
            if (paramTypes.length != params.length) {
                throw new RuntimeException("Parameter count mismatch for " + controllerClass.getName());
            }

            // Instantiate the controller
            constructor.setAccessible(true);
            T controller = (T) constructor.newInstance(params);
            fxmlLoader.setController(controller);

            // Load the FXML and set the scene
            Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
            stage.setScene(scene);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
