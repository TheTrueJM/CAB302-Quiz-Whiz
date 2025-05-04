package ai.tutor.cab302exceptionalhandlers.model;

import java.sql.SQLException;
import java.util.List;

public interface IUserAnswerDAO {
	public void createUserAnswer(UserAnswer userAnswer) throws SQLException;

	public UserAnswer getUserQuestionAnswer(int messageId, int attempt, int questionNumber) throws SQLException;

	public List<UserAnswer> getAllUserQuestionAttempts(int messageId, int questionNumber) throws SQLException;

	public List<UserAnswer> getAllUserQuizAnswers(int messageId, int attempt) throws SQLException;

	public List<UserAnswer> getAllUserQuizAttempts(int messageId) throws SQLException;
}
