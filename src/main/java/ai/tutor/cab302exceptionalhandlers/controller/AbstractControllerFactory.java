package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Quiz;

public abstract class AbstractControllerFactory {
    protected final SQLiteConnection db;

    public AbstractControllerFactory(SQLiteConnection db) {
        this.db = db;
    }

    public abstract AuthController createAuthController(String type);
    public abstract ChatController createChatController(User currentUser);
    public abstract ChatSetupController createChatSetupController(User currentUser, String type);
    public abstract ChatSetupController createChatSetupController(User currentUser, String type, Chat currentChat);
    public abstract UserSettingsController createUserSettingsController(User currentUser);
    public abstract QuizController createQuizController(Quiz quiz, User currentUser);
}
