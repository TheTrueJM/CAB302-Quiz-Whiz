package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;

/**
 * Builder for creating instances of {@link ChatController}.
 * <p>
 * This class is responsible for the construction of {@code ChatController} objects,
 * requiring a currently authenticated user.
 *
 * <p>Usage Example:
 * <pre>
 * ChatController controller = controllerFactory.chatController()
 *                                .currentUser(authenticatedUser)
 *                                .build();
 * </pre>
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.ChatController
 */
public class ChatControllerBuilder extends ControllerBuilder<ChatController> {
    private User authenticatedUser;

    /**
     * Constructs a {@code ChatControllerBuilder}.
     *
     * @param db The {@link SQLiteConnection} to be used for database operations.
     */
    public ChatControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    /**
     * Sets the current authenticated user for the {@link ChatController}.
     *
     * @param user The authenticated {@link User}.
     * @return This {@code ChatControllerBuilder} instance for chaining.
     */
    public ChatControllerBuilder currentUser(User user) {
        this.authenticatedUser = user;
        return this;
    }

    /**
     * Builds a {@link ChatController}.
     *
     * @return A new instance of {@link ChatController}.
     * @throws IllegalStateException if the current user is not set.
     * @throws Exception if any other error occurs during construction (though typically only IllegalStateException is expected from this build).
     */
    @Override
    public ChatController build() throws Exception {
        if (authenticatedUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new ChatController(db, authenticatedUser);
    }
}
