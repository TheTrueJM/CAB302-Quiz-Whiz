package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IQuizQuestionDAO {
	public void createQuizQuestion(QuizQuestion quizQuestion) throws SQLException;

	public QuizQuestion getQuizQuestion(int messageId, int number) throws SQLException;

	public List<QuizQuestion> getAllQuizQuestions(int messageId) throws SQLException;
}
