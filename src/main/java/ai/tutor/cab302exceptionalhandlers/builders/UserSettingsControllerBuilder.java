package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.controller.UserSettingsController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;

import java.sql.SQLException;

/**
 * Builder for creating instances of {@link UserSettingsController}.
 * <p>
 * This class is responsible for the construction of {@code UserSettingsController} objects,
 * requiring the current authenticated {@link User}.
 *
 * <p>Usage Example:
 * <pre>
 * UserSettingsController controller = controllerFactory.userSettingsController()
 *                                        .currentUser(authenticatedUser)
 *                                        .build();
 * </pre>
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.UserSettingsController
 */
public class UserSettingsControllerBuilder extends ControllerBuilder<UserSettingsController> {
    private User currentUser;

    /**
     * Constructs a {@code UserSettingsControllerBuilder}.
     *
     * @param db The {@link SQLiteConnection} to be used for database operations.
     */
    public UserSettingsControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    /**
     * Sets the current authenticated user for the {@link UserSettingsController}.
     *
     * @param user The authenticated {@link User}.
     * @return This {@code UserSettingsControllerBuilder} instance for chaining.
     */
    public UserSettingsControllerBuilder currentUser(User user) {
        this.currentUser = user;
        return this;
    }

    /**
     * Builds a {@link UserSettingsController}.
     *
     * @return A new instance of {@link UserSettingsController}.
     * @throws IllegalStateException if the current user is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     */
    @Override
    public UserSettingsController build() throws IllegalStateException, RuntimeException, SQLException {
        return new UserSettingsController(db, currentUser);
    }
}
