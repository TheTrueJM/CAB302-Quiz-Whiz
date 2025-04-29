package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Message;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;

public class AIController {
    private final OllamaAPI ollamaAPI = new OllamaAPI();
    private final String modelName = "qwen3:4b"; // hardcoded, do we want this???
    private final String currentSystemPrompt;
    private final OllamaChatRequestBuilder ollamaBuilder = OllamaChatRequestBuilder.getInstance(modelName);
    private final HashMap<String, String> prompts = new HashMap<>();
    private boolean verbose = false;

    public AIController() throws IOException {
        this.ollamaAPI.setVerbose(false);
        this.currentSystemPrompt = loadSystemPrompt();
    }

    public boolean isOllamaRunning() {
        try {
            return ollamaAPI.ping();
        } catch ( RuntimeException e) {
            System.err.println("Error pinging Ollama API: " + e.getMessage());
            return false;
        }
    }

    public boolean hasModel() {
        try {
            ollamaAPI.getModelDetails(modelName);
            return true;
        } catch (Exception e) {
            System.err.println("Error checking model: " + e.getMessage());
            return false;
        }
    }

    public String getModelName() {
        return modelName;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    private String loadSystemPrompt() throws IOException {
        String systemTutorPromptPath = "/ai/tutor/cab302exceptionalhandlers/prompts/system_prompt.txt";
        String systemQuizPromptPath = "/ai/tutor/cab302exceptionalhandlers/prompts/quiz_prompt.txt";

        String tutorPrompt = loadPromptFromFile(systemTutorPromptPath);
        String quizPrompt = loadPromptFromFile(systemQuizPromptPath);

        prompts.put("tutor", tutorPrompt);
        prompts.put("quiz", quizPrompt);
        return tutorPrompt;
    }

    private String loadPromptFromFile(String resourcePath) throws IOException {
        try (InputStream is = AIController.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IOException("Prompt file not found in classpath: " + resourcePath);
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private boolean ollamaResultIsNull(OllamaChatRequest ollamaRequest) {
        return ollamaRequest == null || ollamaRequest.getMessages() == null
                || ollamaRequest.getMessages().isEmpty();
    }

    public String generateResponse(List<Message> history, Chat chatConfig, boolean requestQuiz) {
        // TODO: quiz handling
        try {
            String systemPrompt = String.format(
                currentSystemPrompt,
                chatConfig.getResponseAttitude(),
                chatConfig.getQuizDifficulty(),
                chatConfig.getEducationLevel(),
                chatConfig.getStudyArea(),
                chatConfig.getName()
            );

            ollamaBuilder.reset();
            ollamaBuilder.withMessage(OllamaChatMessageRole.SYSTEM, systemPrompt);

            for (Message msg : history) {
                OllamaChatMessageRole role = msg.getFromUser() ? OllamaChatMessageRole.USER : OllamaChatMessageRole.ASSISTANT;
                ollamaBuilder.withMessage(role, msg.getContent());
            }

            OllamaChatRequest ollamaRequest = ollamaBuilder.build();
            OllamaChatResult ollamaResult = ollamaAPI.chat(ollamaRequest);

            if (ollamaResultIsNull(ollamaRequest)) {
                throw new OllamaBaseException("Received null response from Ollama API.");
            }

            String response = ollamaResult.getResponseModel().getMessage().getContent();
            if (verbose) System.out.println(String.format("Response: \n---\n%s\n---", response));

            return response;

        /* Not sure whats the proper way to handle these exceptions */
        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            return "Error generating response: " + e.getMessage();
        }
    }


   // TODO: Quiz
}
