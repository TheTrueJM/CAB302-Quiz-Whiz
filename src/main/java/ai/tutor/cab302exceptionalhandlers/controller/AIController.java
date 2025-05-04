package ai.tutor.cab302exceptionalhandlers.controller;

import ai.tutor.cab302exceptionalhandlers.model.Chat;
import ai.tutor.cab302exceptionalhandlers.model.Message;

import io.github.ollama4j.OllamaAPI;
import io.github.ollama4j.exceptions.OllamaBaseException;
import io.github.ollama4j.models.chat.*;
import io.github.ollama4j.utils.Options;
import io.github.ollama4j.utils.OptionsBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;

import com.google.gson.Gson;

public class AIController {
    private final OllamaAPI ollamaAPI;
    private final String modelName = "qwen3:4b"; // hardcoded, do we want this???
    private final String currentSystemPrompt;
    private final OllamaChatRequestBuilder ollamaBuilder = OllamaChatRequestBuilder.getInstance(modelName);
    private final HashMap<String, String> prompts = new HashMap<>();
    private final Gson gson = new Gson();
    private boolean verbose = false;

    /* Model Settings (change if you're using a different model) */
    private final float temperature = 0.9f;
    private final int numPredict = -2;
    private final int numCtx = 40960;

    public class ModelResponseFormat {
        @SuppressWarnings("unused")
        public final boolean userWantsQuiz;

        @SuppressWarnings("unused")
        public final String response;

        @SuppressWarnings("unused")
        public final boolean isError;

        public final QuizFormat[] quizzes;

        public ModelResponseFormat(boolean userWantsQuiz, boolean isError, String response, QuizFormat[] quizzes) {
            this.userWantsQuiz = userWantsQuiz;
            this.response = response;
            this.isError = isError;
            this.quizzes = quizzes;
        }
    }

    public class QuizFormat {
        @SuppressWarnings("unused")
        public final String question;

        @SuppressWarnings("unused")
        public final String answer;

        @SuppressWarnings("unused")
        public final String[] answerOptions;

        @SuppressWarnings("unused")
        public final String answerSolutions;

        public QuizFormat(String question, String answer, String[] options, String answerSolutions) {
            this.question = question;
            this.answer = answer;
            this.answerOptions = options;
            this.answerSolutions = answerSolutions;
        }
    }

    public AIController() throws IOException {
        this.ollamaAPI = new OllamaAPI();
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

    public ModelResponseFormat generateResponse(List<Message> history, Chat chatConfig, boolean requestQuiz) {
        // TODO: quiz handling
        try {
            String systemPrompt = String.format(
                currentSystemPrompt,
                chatConfig.getName(),
                chatConfig.getResponseAttitude(),
                chatConfig.getQuizDifficulty(),
                chatConfig.getEducationLevel(),
                chatConfig.getStudyArea()
            );

            Options options = new OptionsBuilder()
                .setTemperature(this.temperature)
                .setNumPredict(this.numPredict)
                .setNumCtx(this.numCtx)
                .build();

            ollamaBuilder.reset();
            ollamaBuilder.withMessage(OllamaChatMessageRole.SYSTEM, systemPrompt);
            ollamaBuilder.withOptions(options);

            for (Message msg : history) {
                OllamaChatMessageRole role = msg.getFromUser() ? OllamaChatMessageRole.USER : OllamaChatMessageRole.ASSISTANT;
                ollamaBuilder.withMessage(role, msg.getContent());

                if (verbose) {
                    System.out.println(String.format(
                        "%s: \n---\n%s\n---", role.toString(), msg.getContent()
                    ));
                }
            }

            OllamaChatRequest ollamaRequest = ollamaBuilder.build();
            OllamaChatResult ollamaResult = ollamaAPI.chat(ollamaRequest);

            if (ollamaResultIsNull(ollamaRequest)) {
                throw new OllamaBaseException("Received null response from Ollama API.");
            }

            String response = ollamaResult.getResponseModel().getMessage().getContent();
            if (verbose) {
                System.out.println(String.format(
                    "AI Response: \n---\n%s\n---", response
                ));
            }

            /* Format out the <thinking> tokens and everything between that */
            String[] responseParts = response.split("<think>");
            StringBuilder formattedResponse = new StringBuilder();
            for (String part : responseParts) {
                    if (part.contains("</think>")) {
                    String[] subParts = part.split("</think>");
                    formattedResponse.append(subParts[1]);
                } else {
                    formattedResponse.append(part);
                }
            }

            response = formattedResponse.toString();

            ModelResponseFormat responseFormat = gson.fromJson(response, ModelResponseFormat.class);
            if (responseFormat == null) {
                System.err.println("Error: Unable to parse response from AI.");
                return new ModelResponseFormat(
                    false,
                    false,
                    "Error: Unable to parse response from AI.",
                    null
                );
            }

            return responseFormat;
        /* Not sure whats the proper way to handle these exceptions */
        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            return new ModelResponseFormat(
                false,
                true,
                "Error: Unable to generate response from AI.",
                null
            );
        }
    }


   // TODO: Quiz
}
