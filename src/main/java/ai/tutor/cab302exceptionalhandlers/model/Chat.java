package ai.tutor.cab302exceptionalhandlers.model;

public class Chat {
    private int id;
    private final int userId;
    private String name;
    private String responseAttitude;
    private String quizDifficulty;
    private String educationLevel;
    private String studyArea;



    public Chat(int userId, String name, String responseAttitude, String quizDifficulty, String educationLevel, String studyArea) {
        this.userId = userId;
        this.name = name;
        this.responseAttitude = responseAttitude;
        this.quizDifficulty = quizDifficulty;
        this.educationLevel = educationLevel;
        this.studyArea = studyArea;
    }


    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getResponseAttitude() { return responseAttitude; }

    public void setResponseAttitude(String responseAttitude) { this.responseAttitude = responseAttitude; }

    public String getQuizDifficulty() { return quizDifficulty; }

    public void setQuizDifficulty(String quizDifficulty) { this.quizDifficulty = quizDifficulty; }

    public String getEducationLevel() { return educationLevel; }

    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public String getStudyArea() { return studyArea; }

    public void setStudyArea(String studyArea) { this.studyArea = studyArea; }

    @Override
    public String toString() {
        return name != null ? name : "Unnamed Chat";
    }
}
