package ai.tutor.cab302exceptionalhandlers.model;

/**
 * Represents a chat belonging to a user.
 * <p>
 *
 * @author Joshua M.
 * @see ai.tutor.cab302exceptionalhandlers.model.User
 * @see ai.tutor.cab302exceptionalhandlers.model.ChatDAO
 */
public class Chat {
    public static final int MIN_QUIZ_LENGTH = 1;
    public static final int MAX_QUIZ_LENGTH = 10;

    private int id;
    private final int userId;
    private String name;
    private String responseAttitude;
    private String quizDifficulty;
    private int quizLength;
    private String educationLevel;
    private String studyArea;


    /**
     * Constructs a Chat object.
     *
     * @param userId The user id that this chat belongs to, must be greater than 0
     * @param name The name of the chat, must be 1-50 characters
     * @param responseAttitude The response attitude for this chat's AI, must be 1-25 characters
     * @param quizDifficulty The difficulty for this chat's Quizzes, must be 1-25 characters
     * @param quizLength The difficulty for this chat's Quizzes, must be between {@value MIN_QUIZ_LENGTH}-{@value MAX_QUIZ_LENGTH}
     * @param educationLevel The education level for this chat, must be 50 characters or less
     * @param studyArea The study area for this chat, must be 50 characters or less
     * @throws IllegalArgumentException if any of the parameters are invalid
     */
    public Chat(int userId, String name, String responseAttitude, String quizDifficulty, int quizLength, String educationLevel, String studyArea) throws IllegalArgumentException {
        if (userId < 1) { throw new IllegalArgumentException("Invalid User Id: Must be greater than 1"); }
        this.userId = userId;

        setName(name);
        setResponseAttitude(responseAttitude);
        setQuizDifficulty(quizDifficulty);
        setQuizLength(quizLength);
        setEducationLevel(educationLevel);
        setStudyArea(studyArea);
    }


    public int getId() { return id; }

    public void setId(int id) throws IllegalArgumentException {
        if (id < 1) {
            throw new IllegalArgumentException("Invalid Id: Must be greater than 1");
        }

        this.id = id;
    }

    public int getUserId() { return userId; }

    public String getName() { return name; }

    public void setName(String name) throws IllegalArgumentException {
        if (name == null || name.isEmpty() || 50 < name.length()) {
            throw new IllegalArgumentException("Invalid Chat Name: Must only contain 1-50 characters");
        }

        this.name = name;
    }

    public String getResponseAttitude() { return responseAttitude; }

    public void setResponseAttitude(String responseAttitude) throws IllegalArgumentException {
        if (responseAttitude == null || responseAttitude.isEmpty() || 25 < responseAttitude.length()) {
            throw new IllegalArgumentException("Invalid Response Attitude: Must only contain 1-25 characters");
        }

        this.responseAttitude = responseAttitude;
    }

    public String getQuizDifficulty() { return quizDifficulty; }

    public void setQuizDifficulty(String quizDifficulty) throws IllegalArgumentException {
        if (quizDifficulty == null || quizDifficulty.isEmpty() || 25 < quizDifficulty.length()) {
            throw new IllegalArgumentException("Invalid Quiz Difficulty: Must only contain 1-25 characters");
        }

        this.quizDifficulty = quizDifficulty;
    }

    public int getQuizLength() { return quizLength; }

    public void setQuizLength(int quizLength) throws IllegalArgumentException {
        if (quizLength < MIN_QUIZ_LENGTH || MAX_QUIZ_LENGTH < quizLength) {
            throw new IllegalArgumentException("Invalid Quiz Length: Must between " + MIN_QUIZ_LENGTH + " and " + MAX_QUIZ_LENGTH);
        }

        this.quizLength = quizLength;
    }

    public String getEducationLevel() { return educationLevel; }

    public void setEducationLevel(String educationLevel) throws IllegalArgumentException {
        if (educationLevel != null && 50 < educationLevel.length()) {
            throw new IllegalArgumentException("Invalid Education Level: Must be less than 50 characters");
        }

        this.educationLevel = educationLevel != null && !educationLevel.isEmpty() ? educationLevel : null;
    }

    public String getStudyArea() { return studyArea; }

    public void setStudyArea(String studyArea) throws IllegalArgumentException {
        if (studyArea != null && 50 < studyArea.length()) {
            throw new IllegalArgumentException("Invalid Study Area: Must be less than 50 characters");
        }

        this.studyArea = studyArea != null && !studyArea.isEmpty() ? studyArea : null;
    }
}
