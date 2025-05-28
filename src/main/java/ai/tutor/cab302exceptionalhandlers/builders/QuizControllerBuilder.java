package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.controller.QuizController;
import ai.tutor.cab302exceptionalhandlers.model.Quiz;

import java.sql.SQLException;

/**
 * Builder for creating instances of {@link QuizController}.
 * <p>
 * This class is responsible for the construction of {@code QuizController} objects,
 * requiring a specific {@link Quiz} and the current authenticated {@link User}.
 *
 * <p>Usage Example:
 * <pre>
 * QuizController controller = controllerFactory.quizController()
 *                                .quiz(selectedQuiz)
 *                                .currentUser(authenticatedUser)
 *                                .build();
 * </pre>
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.QuizController
 */
public class QuizControllerBuilder extends ControllerBuilder<QuizController> {
    private Quiz chosenQuiz;
    private User currentUser;

    /**
     * Constructs a {@code QuizControllerBuilder}.
     *
     * @param db The {@link SQLiteConnection} to be used for database operations.
     */
    public QuizControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    /**
     * Sets the quiz for the {@link QuizController}.
     *
     * @param quiz The {@link Quiz} to be associated with the controller.
     * @return This {@code QuizControllerBuilder} instance for chaining.
     */
    public QuizControllerBuilder quiz(Quiz quiz) {
        this.chosenQuiz = quiz;
        return this;
    }

    /**
     * Sets the current authenticated user for the {@link QuizController}.
     *
     * @param user The authenticated {@link User}.
     * @return This {@code QuizControllerBuilder} instance for chaining.
     */
    public QuizControllerBuilder currentUser(User user) {
        this.currentUser = user;
        return this;
    }

    /**
     * Builds a {@link QuizController}.
     *
     * @return A new instance of {@link QuizController}.
     * @throws IllegalStateException if the quiz or current user is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     */
    @Override
    public QuizController build() throws IllegalStateException, RuntimeException, SQLException {
        if (chosenQuiz == null) {
            throw new IllegalStateException("Quiz must be set");
        }
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new QuizController(db, chosenQuiz, currentUser);
    }
}
