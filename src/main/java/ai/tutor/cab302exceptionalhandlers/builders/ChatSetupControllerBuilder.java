package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.types.ChatSetupType;
import ai.tutor.cab302exceptionalhandlers.controller.ChatCreateController;
import ai.tutor.cab302exceptionalhandlers.controller.ChatSetupController;
import ai.tutor.cab302exceptionalhandlers.controller.ChatUpdateController;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import java.sql.SQLException;

/**
 * Builder for creating instances of {@link ChatSetupController}.
 * <p>
 * This class allows for the construction of either a {@link ChatCreateController}
 * or a {@link ChatUpdateController} based on the specified {@link ChatSetupType}.
 * It requires the current user and, for updates, the specific chat to be modified.
 *
 * <p>Usage Example (Create):
 * <pre>
 * ChatSetupController createController = controllerFactory.chatSetupController()
 *                                          .currentUser(user)
 *                                          .type(ChatSetupType.CREATE)
 *                                          .build();
 * </pre>
 *
 * <p>Usage Example (Update):
 * <pre>
 * ChatSetupController updateController = controllerFactory.chatSetupController()
 *                                          .currentUser(user)
 *                                          .type(ChatSetupType.UPDATE)
 *                                          .currentChat(chatToUpdate)
 *                                          .build();
 * </pre>
 *
 * @author Justin.
 * @see ai.tutor.cab302exceptionalhandlers.builders.ControllerBuilder
 * @see ai.tutor.cab302exceptionalhandlers.controller.ChatCreateController
 * @see ai.tutor.cab302exceptionalhandlers.controller.ChatUpdateController
 */
public class ChatSetupControllerBuilder extends ControllerBuilder<ChatSetupController> {
    private User currentUser;
    private ChatSetupType setupType;
    private Chat currentChat;

    /**
     * Constructs a {@code ChatSetupControllerBuilder}.
     *
     * @param db The {@link SQLiteConnection} to be used for database operations.
     */
    public ChatSetupControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    /**
     * Sets the current authenticated user.
     *
     * @param user The authenticated {@link User}.
     * @return This {@code ChatSetupControllerBuilder} instance for chaining.
     */
    public ChatSetupControllerBuilder currentUser(User user) {
        this.currentUser = user;
        return this;
    }

    /**
     * Sets the type of chat setup operation (CREATE or UPDATE).
     *
     * @param type The {@link ChatSetupType} specifying the operation.
     * @return This {@code ChatSetupControllerBuilder} instance for chaining.
     */
    public ChatSetupControllerBuilder type(ChatSetupType type) {
        this.setupType = type;
        return this;
    }

    /**
     * Sets the current chat, required for update operations.
     *
     * @param chat The {@link Chat} to be updated.
     * @return This {@code ChatSetupControllerBuilder} instance for chaining.
     */
    public ChatSetupControllerBuilder currentChat(Chat chat) {
        this.currentChat = chat;
        return this;
    }

    /**
     * Builds a {@link ChatSetupController} (either {@link ChatCreateController} or {@link ChatUpdateController})
     * based on the specified type and parameters.
     *
     * @return An instance of {@link ChatSetupController}.
     * @throws IllegalStateException if required parameters (currentUser, setupType, or currentChat for UPDATE) are not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     */
    @Override
    public ChatSetupController build() throws IllegalStateException, RuntimeException, SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        if (setupType == null) {
            throw new IllegalStateException("Setup type must be set");
        }

        return switch (setupType) {
            case CREATE -> buildCreate();
            case UPDATE -> buildUpdate();
        };
    }

    /**
     * Builds a {@link ChatCreateController}.
     * Requires {@code currentUser} to be set.
     *
     * @return A new instance of {@link ChatCreateController}.
     * @throws IllegalStateException if the current user is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     */
    public ChatCreateController buildCreate() throws IllegalStateException, RuntimeException, SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new ChatCreateController(db, currentUser);
    }

    /**
     * Builds a {@link ChatUpdateController} for the given chat.
     * Requires {@code currentUser} and {@code currentChat} to be set.
     *
     * @return A new instance of {@link ChatUpdateController}.
     * @throws IllegalStateException if the current user or selected chat is not set.
     * @throws RuntimeException if a database connection error occurs during controller construction.
     * @throws SQLException if a database access error occurs during controller construction.
     */
    public ChatUpdateController buildUpdate() throws IllegalStateException, RuntimeException, SQLException {
        return new ChatUpdateController(db, currentUser, currentChat);
    }
}
