package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.controller.ChatController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import java.io.IOException;
import java.sql.SQLException;

public class ChatControllerBuilder extends ControllerBuilder<ChatController> {
    private User authenticatedUser;

    public ChatControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    public ChatControllerBuilder currentUser(User user) {
        this.authenticatedUser = user;
        return this;
    }

    @Override
    public ChatController build() throws Exception {
        if (authenticatedUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new ChatController(db, authenticatedUser);
    }
}
