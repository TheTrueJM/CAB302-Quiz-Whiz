package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.types.ChatSetupType;
import ai.tutor.cab302exceptionalhandlers.controller.ChatCreateController;
import ai.tutor.cab302exceptionalhandlers.controller.ChatSetupController;
import ai.tutor.cab302exceptionalhandlers.controller.ChatUpdateController;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import java.sql.SQLException;

public class ChatSetupControllerBuilder extends ControllerBuilder<ChatSetupController> {
    private User currentUser;
    private ChatSetupType setupType;
    private Chat currentChat;

    public ChatSetupControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    public ChatSetupControllerBuilder currentUser(User user) {
        this.currentUser = user;
        return this;
    }

    public ChatSetupControllerBuilder type(ChatSetupType type) {
        this.setupType = type;
        return this;
    }

    public ChatSetupControllerBuilder currentChat(Chat chat) {
        this.currentChat = chat;
        return this;
    }

    @Override
    public ChatSetupController build() throws Exception {
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        if (setupType == null) {
            throw new IllegalStateException("Setup type must be set");
        }

        return switch (setupType) {
            case CREATE -> buildCreate();
            case UPDATE -> {
                if (currentChat == null) {
                    throw new IllegalStateException("Selected chat must be set for update type");
                }
                yield buildUpdate(currentChat);
            }
        };
    }

    public ChatCreateController buildCreate() throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new ChatCreateController(db, currentUser);
    }

    public ChatUpdateController buildUpdate(Chat selectedChat) throws SQLException {
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        this.currentChat = selectedChat;
        return new ChatUpdateController(db, currentUser, selectedChat);
    }
}
