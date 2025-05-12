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
    public static <T> void loadPage(String fileName, Class<T> controllerClass, Stage stage, Object[] params) {
        FXMLLoader fxmlLoader = new FXMLLoader(
                QuizWhizApplication.class.getResource(fileName)
        );

        try {
            // Find matching constructor
            Constructor<?> matchingConstructor = null;
            for (Constructor<?> constructor : controllerClass.getDeclaredConstructors()) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == params.length) {
                    boolean matches = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (params[i] != null && !paramTypes[i].isAssignableFrom(params[i].getClass())) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        matchingConstructor = constructor;
                        break;
                    }
                }
            }

            // Validate constructor found
            if (matchingConstructor == null) {
                throw new RuntimeException("No matching constructor found for " + controllerClass.getName());
            }

            // Instantiate the controller
            matchingConstructor.setAccessible(true);
            T controller = (T) matchingConstructor.newInstance(params);
            fxmlLoader.setController(controller);

            // Load the FXML and set the scene
            Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
            stage.setScene(scene);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
