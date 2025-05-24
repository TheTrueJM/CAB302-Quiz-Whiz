package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.controller.UserSettingsController;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import java.sql.SQLException;

public class UserSettingsControllerBuilder extends ControllerBuilder<UserSettingsController> {
    private User currentUser;

    public UserSettingsControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    public UserSettingsControllerBuilder currentUser(User user) {
        this.currentUser = user;
        return this;
    }

    @Override
    public UserSettingsController build() throws Exception {
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new UserSettingsController(db, currentUser);
    }
}
