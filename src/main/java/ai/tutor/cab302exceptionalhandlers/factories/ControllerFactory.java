package ai.tutor.cab302exceptionalhandlers.factories;

import ai.tutor.cab302exceptionalhandlers.builders.AuthControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.ChatControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.ChatSetupControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.QuizControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.builders.UserSettingsControllerBuilder;
import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;

public class ControllerFactory extends AbstractControllerFactory {
    public ControllerFactory(SQLiteConnection db) {
        super(db);
    }

    public AuthControllerBuilder authController() {
        return new AuthControllerBuilder(db);
    }

    public ChatControllerBuilder chatController() {
        return new ChatControllerBuilder(db);
    }

    public ChatSetupControllerBuilder chatSetupController() {
        return new ChatSetupControllerBuilder(db);
    }

    public UserSettingsControllerBuilder userSettingsController() {
        return new UserSettingsControllerBuilder(db);
    }

    public QuizControllerBuilder quizController() {
        return new QuizControllerBuilder(db);
    }
}
