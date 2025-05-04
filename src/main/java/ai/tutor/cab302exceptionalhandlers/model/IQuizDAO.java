package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IQuizDAO {
	public void createQuiz(Quiz quiz) throws SQLException;

	public Quiz getQuiz(int messageId) throws SQLException;

	public List<Quiz> getAllChatQuizzes(int chatId) throws SQLException;

	public List<Quiz> getAllUserQuizzes(int userId) throws SQLException;
}
