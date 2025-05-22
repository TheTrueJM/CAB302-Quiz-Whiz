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
    private final String modelName = "qwen3:4b";
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
        public final String response;

        @SuppressWarnings("unused")
        public final boolean isError;

        public final QuizFormat[] quizzes;

        public ModelResponseFormat(boolean isError, String response, QuizFormat[] quizzes) {
            this.response = response;
            this.isError = isError;
            this.quizzes = quizzes;
        }

        public String getQuizTitle() {
            if (quizzes != null && quizzes.length > 0) {
                return quizzes[0].getQuizTitle();
            }
            return null;
        }

        public List<Question> getQuizQuestions() {
            if (quizzes != null && quizzes.length > 0) {
                return List.of(quizzes[0].questions);
            }
            return null;
        }
    }

    public class QuizFormat {
        @SuppressWarnings("unused")
        private final String quizTitle;

        @SuppressWarnings("unused")
        private final Question[] questions;

        public QuizFormat(String quizTitle, Question[] questions) {
            this.quizTitle = quizTitle;
            this.questions = questions;
        }

        public String getQuizTitle() {
            return quizTitle;
        }

        public Question[] getQuestions() {
            return questions;
        }
    }

    public class Question {
        @SuppressWarnings("unused")
        private final int questionNumber;

        @SuppressWarnings("unused")
        private final String questionContent;

        @SuppressWarnings("unused")
        private final Option[] options;

        public Question(int questionNumber, String questionText, Option[] options) {
            this.questionNumber = questionNumber;
            this.questionContent = questionText;
            this.options = options;
        }

        public int getQuestionNumber() {
            return questionNumber;
        }

        public String getQuestionContent() {
            return questionContent;
        }

        public Option[] getOptions() {
            return options;
        }
    }

    public class Option {
        @SuppressWarnings("unused")
        private final String optionLetter;

        @SuppressWarnings("unused")
        private final String optionText;

        @SuppressWarnings("unused")
        private final boolean isAnswer;

        public Option(String optionLetter, String optionText, boolean isAnswer) {
            this.optionLetter = optionLetter;
            this.optionText = optionText;
            this.isAnswer = isAnswer;
        }

        public String getOptionLetter() {
            return optionLetter;
        }

        public String getOptionText() {
            return optionText;
        }

        public boolean isAnswer() {
            return isAnswer;
        }
    }

    public AIController() throws IOException {
        this.ollamaAPI = new OllamaAPI("https://ollama.jushbjj.com");
        this.ollamaAPI.setVerbose(false);
        loadPrompts();
    }

    public static boolean validateQuizResponse(ModelResponseFormat response) {
        if (response == null || response.isError || response.quizzes == null || response.quizzes.length == 0) {
            return false;
        }

        for (QuizFormat quiz : response.quizzes) {
            if (quiz == null ||
                quiz.getQuizTitle() == null || quiz.getQuizTitle().isEmpty() ||
                quiz.getQuestions() == null || quiz.getQuestions().length == 0) {
                return false;
            }

            for (Question question : quiz.getQuestions()) { // Use getter
                if (question == null ||
                    question.questionNumber < 1 ||
                    question.getQuestionContent() == null || question.getQuestionContent().isEmpty() ||
                    question.options == null || question.options.length == 0) {
                    return false;
                }

                boolean hasCorrectOption = false;
                for (Option option : question.options) {
                    if (option == null ||
                        option.optionLetter == null || option.optionLetter.isEmpty() ||
                        option.optionText == null || option.optionText.isEmpty()) {
                        return false;
                    }

                    hasCorrectOption |= option.isAnswer;
                    if (hasCorrectOption) {
                        break;
                    }
                }

                if (!hasCorrectOption) {
                    return false;
                }
            }
        }

        return true;
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

    private void loadPrompts() throws IOException {
        String systemTutorPromptPath = "/ai/tutor/cab302exceptionalhandlers/prompts/system_prompt.txt";
        String systemQuizPromptPath = "/ai/tutor/cab302exceptionalhandlers/prompts/quiz_system_prompt.txt";

        String tutorPrompt = loadPromptFromFile(systemTutorPromptPath);
        String quizPrompt = loadPromptFromFile(systemQuizPromptPath);

        prompts.put("tutor", tutorPrompt);
        prompts.put("quiz", quizPrompt);
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

    public ModelResponseFormat generateResponse(List<Message> history, Chat chatConfig, boolean isQuizMode) {
        try {
            String promptTemplate = isQuizMode ? prompts.get("quiz") : prompts.get("tutor");

            String systemPrompt = String.format(
                promptTemplate,
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

            // do NOT remove `\n` or it will mess up the AI response
            response = formattedResponse.toString();

            if (isQuizMode) {
                return processQuizResponse(response);
            } else {
                return processChatResponse(response);
            }

        } catch (Exception e) {
            System.err.println("Error generating response: " + e.getMessage());
            return new ModelResponseFormat(
                true,
                "Error: Unable to generate response from AI.",
                null
            );
        }
    }

    private ModelResponseFormat processQuizResponse(String response) {
        try {
            String jsonContent = response;
            if (response.contains("```json")) {
                jsonContent = response.substring(response.indexOf("```json") + 7);
                jsonContent = jsonContent.substring(0, jsonContent.indexOf("```"));
                jsonContent = jsonContent.trim();
            } else if (response.contains("```")) {
                jsonContent = response.substring(response.indexOf("```") + 3);
                jsonContent = jsonContent.substring(0, jsonContent.indexOf("```"));
                jsonContent = jsonContent.trim();
            }

            QuizFormat quizData = gson.fromJson(jsonContent, QuizFormat.class);

            return new ModelResponseFormat(
                false,
                response,
                new QuizFormat[] { quizData }
            );
        } catch (Exception e) {
            System.err.println("Error parsing quiz response: " + e.getMessage());
            return new ModelResponseFormat(
                true,
                "Error: Unable to parse quiz response from AI. " + e.getMessage(),
                null
            );
        }
    }

    private ModelResponseFormat processChatResponse(String response) {
        try {
            ModelResponseFormat responseFormat = gson.fromJson(response, ModelResponseFormat.class);
            if (responseFormat == null) {
                System.err.println("Error: Unable to parse response from AI.");
                return new ModelResponseFormat(
                    false,
                    "Error: Unable to parse response from AI.",
                    null
                );
            }
            return responseFormat;
        } catch (Exception e) {
            return new ModelResponseFormat(
                false,
                response,
                null
            );
        }
    }
}
