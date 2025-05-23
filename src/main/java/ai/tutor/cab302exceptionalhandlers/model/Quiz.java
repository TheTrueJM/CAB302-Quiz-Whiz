package ai.tutor.cab302exceptionalhandlers.model;

public class Quiz {
    private final int messageId;
    private final String name;
    private final String difficulty;


    public Quiz(int messageId, String name, String difficulty) throws IllegalArgumentException {
        if (messageId < 1) { throw new IllegalArgumentException("Invalid Message Id: Must be greater than 1"); }
        this.messageId = messageId;

        if (name == null || name.isEmpty() || 50 < name.length()) { throw new IllegalArgumentException("Invalid Quiz Name: Must only contain 1-50 characters"); }
        this.name = name;

        if (difficulty == null || difficulty.isEmpty() || 25 < difficulty.length()) { throw new IllegalArgumentException("Invalid Quiz Difficulty: Must only contain 1-25 characters"); }
        this.difficulty = difficulty;
    }


    public int getMessageId() { return messageId; }

    public String getName() { return name; }

    public String getDifficulty() { return difficulty; }
}
