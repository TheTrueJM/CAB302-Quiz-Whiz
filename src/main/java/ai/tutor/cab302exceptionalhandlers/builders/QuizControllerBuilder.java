package ai.tutor.cab302exceptionalhandlers.builders;

import ai.tutor.cab302exceptionalhandlers.model.SQLiteConnection;
import ai.tutor.cab302exceptionalhandlers.model.User;
import ai.tutor.cab302exceptionalhandlers.controller.QuizController;
import ai.tutor.cab302exceptionalhandlers.model.Quiz;

public class QuizControllerBuilder extends ControllerBuilder<QuizController> {
    private Quiz chosenQuiz;
    private User currentUser;

    public QuizControllerBuilder(SQLiteConnection db) {
        super(db);
    }

    public QuizControllerBuilder quiz(Quiz quiz) {
        this.chosenQuiz = quiz;
        return this;
    }

    public QuizControllerBuilder currentUser(User user) {
        this.currentUser = user;
        return this;
    }

    @Override
    public QuizController build() throws Exception {
        if (chosenQuiz == null) {
            throw new IllegalStateException("Quiz must be set");
        }
        if (currentUser == null) {
            throw new IllegalStateException("Current user must be set");
        }
        return new QuizController(db, chosenQuiz, currentUser);
    }
}
