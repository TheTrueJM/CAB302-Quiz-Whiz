package ai.tutor.cab302exceptionalhandlers.model;

import ai.tutor.cab302exceptionalhandlers.QuizWhizApplication;
import java.util.Arrays;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class Utils {
	// Generic function that loads fxml page and controller class
	// TODO: Move to accessible place
	public static <T> void loadPage(String fileName, Class<T> controllerClass, Stage stage, Object[] params) {
		FXMLLoader fxmlLoader = new FXMLLoader(QuizWhizApplication.class.getResource(fileName));

		try {
			Class<T>[] paramTypes = Arrays.stream(params).map(Object::getClass).toArray(Class[]::new);

			T controller = (T) controllerClass.getDeclaredConstructor(paramTypes).newInstance(params);
			fxmlLoader.setController(controller);

			Scene scene = new Scene(fxmlLoader.load(), QuizWhizApplication.WIDTH, QuizWhizApplication.HEIGHT);
			// Get the Stage from the event
			stage.setScene(scene);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
