package ai.tutor.cab302exceptionalhandlers.factories;

import ai.tutor.cab302exceptionalhandlers.builders.AuthControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.ChatControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.ChatSetupControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.QuizControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.UserSettingsControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

/**
 * Concrete factory for creating various controller builders.
 * <p>
 * This class extends {@link AbstractControllerFactory} and provides methods
 * to obtain builders for different types of controllers used in the application.
 * Each builder is initialized with the {@link SQLiteConnection} provided to this factory.
 *
 * <p>Usage Example:
 * <pre>
 * SQLiteConnection dbConnection = new SQLiteConnection("database.db");
 * ControllerFactory factory = new ControllerFactory(dbConnection);
 *
 * AuthControllerBuilder authBuilder = factory.authController();
 * // ... configure and build AuthController
 *
 * ChatControllerBuilder chatBuilder = factory.chatController();
 * // ... configure and build ChatController
 * </pre>
 *
 * @see ai.tutor.cab302exceptionalhandlers.factories.AbstractControllerFactory
 * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
 */
public class ControllerFactory extends AbstractControllerFactory {
    /**
     * Constructs a {@code ControllerFactory}.
     *
     * @param db The {@link SQLiteConnection} to be used by all controller builders
     *           created by this factory.
     */
    public ControllerFactory(SQLiteConnection db) {
        super(db);
    }

    /**
     * Creates and returns an {@link AuthControllerBuilder}.
     *
     * @return A new instance of {@link AuthControllerBuilder}.
     */
    public AuthControllerBuilder authController() {
        return new AuthControllerBuilder(db);
    }

    /**
     * Creates and returns a {@link ChatControllerBuilder}.
     *
     * @return A new instance of {@link ChatControllerBuilder}.
     */
    public ChatControllerBuilder chatController() {
        return new ChatControllerBuilder(db);
    }

    /**
     * Creates and returns a {@link ChatSetupControllerBuilder}.
     *
     * @return A new instance of {@link ChatSetupControllerBuilder}.
     */
    public ChatSetupControllerBuilder chatSetupController() {
        return new ChatSetupControllerBuilder(db);
    }

    /**
     * Creates and returns a {@link UserSettingsControllerBuilder}.
     *
     * @return A new instance of {@link UserSettingsControllerBuilder}.
     */
    public UserSettingsControllerBuilder userSettingsController() {
        return new UserSettingsControllerBuilder(db);
    }

    /**
     * Creates and returns a {@link QuizControllerBuilder}.
     *
     * @return A new instance of {@link QuizControllerBuilder}.
     */
    public QuizControllerBuilder quizController() {
        return new QuizControllerBuilder(db);
    }
}
