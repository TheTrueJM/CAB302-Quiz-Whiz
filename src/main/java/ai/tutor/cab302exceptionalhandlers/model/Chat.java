package ai.tutor.cab302exceptionalhandlers.model;

public class Chat {
    private int id;
    private final int userId;
    private String name;
    private String educationLevel;


    public Chat(int userId, String name, String educationLevel) {
        this.userId = userId;
        this.name = name;
        this.educationLevel = educationLevel;
    }


    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }

    public String getName() { return name; }

    public void setName(String username) { this.name = name; }

    public String getEducationLevel() { return educationLevel; }

    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }
}
