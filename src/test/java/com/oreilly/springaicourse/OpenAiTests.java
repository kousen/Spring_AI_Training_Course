package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.StructuredOutputValidationAdvisor;
import org.springframework.ai.chat.client.advisor.toolsearch.ToolSearchToolCallingAdvisor;
import org.springframework.ai.tool.toolsearch.index.regex.RegexToolIndex;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("unused")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiTests {

    @Value("classpath:movie_prompt.st")
    private Resource promptTemplate;

    @Value("classpath:bowl_of_fruit.png")
    private Resource imageResource;

    @Autowired
    private OpenAiChatModel model;

    @Autowired
    private ChatMemory memory;

    private ChatClient chatClient;

    private ChatClient evaluator;

    @BeforeEach
    void setUp() {
        // Use builder to add default advisors
//        chatClient = ChatClient.builder(model)
//                .defaultAdvisors(new SimpleLoggerAdvisor())
//                .defaultAdvisors(
//                        new SimpleLoggerAdvisor(),
//                        MessageChatMemoryAdvisor.builder(memory).build())
//                .build();

        // Default model
        chatClient = ChatClient.builder(model)
//                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        // "nano" model for evaluation
        evaluator = chatClient.mutate()
                .defaultOptions(OpenAiChatOptions.builder().model("gpt-5-nano"))
//                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();
    }

    private double evaluateAnswer(String question, String answer) {
        record AnswerQuality(String question, String answer, double score) {}

        String evaluationPrompt = """
                Evaluate the answer to the question
                in terms of its relevance and correctness.
                Question: {question}
                Answer: {answer}
                Provide a score between 0.0 and 1.0,
                where 0.0 is completely wrong and 1.0 is perfect.
                """;

        AnswerQuality answerQuality = evaluator.prompt()
                .user(u -> u.text(evaluationPrompt)
                        .param("question", question)
                        .param("answer", answer))
                .call()
                .entity(AnswerQuality.class);

        assertNotNull(answerQuality);
        NumberFormat numberFormat = NumberFormat.getPercentInstance();
        System.out.println("Correctness probability: " + numberFormat.format(answerQuality.score()));
        return answerQuality.score;
    }

    @Test
    void simpleQuery() {
        String question = "Why is the sky blue?";
        String response = chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor()) // add advisor to existing chat client
                .user(question)
                .call()
                .content();
        System.out.println(response);
        assertTrue(evaluateAnswer("Why is the sky blue?", response) > 0.8);
    }

    @Test
    void simpleQueryRespondLikeAPirate() {
        String question = "How many r's are in the word 'strawberry'?";
        String response = chatClient.prompt()
                .system("You are a helpful assistant that responds like a pirate.")
                .user(question)
                .call()
                .content();
        System.out.println(response);
        assertTrue(evaluateAnswer(question, response) > 0.8);
    }

    @Test
    void simpleQueryWithChatResponse() {
        ChatResponse response = chatClient.prompt()
                .user("Why is the sky blue?")
                .call()
                .chatResponse();
        assertNotNull(response);
        System.out.println("Model: " + response.getMetadata().getModel());
        System.out.println("Usage: " + response.getMetadata().getUsage());
        System.out.println("Response: " + response.getResult().getOutput().getText());
    }

    @Test
    void streamingChatCountDownLatch() throws InterruptedException {
        Flux<String> output = chatClient.prompt()
                .user("Why is the sky blue?")
                .stream()
                .content();

        var latch = new CountDownLatch(1);
        output.subscribe(
                System.out::println,
                e -> {
                    System.out.println("Error: " + e.getMessage());
                    latch.countDown();
                },
                () -> {
                    System.out.println("Completed");
                    latch.countDown();
                }
        );
        latch.await();
    }

    @Test
    void streamingChatDoOnNext() {
        Flux<String> output = chatClient.prompt()
                .user("Why is the sky blue?")
                .stream()
                .content();

        output.doOnNext(System.out::println)
                .doOnError(e -> System.out.println("Error: " + e.getMessage()))
                .doOnCancel(() -> System.out.println("Cancelled"))
                .doOnComplete(() -> System.out.println("Completed"))
                .blockLast();
    }

    @Test // Note: Requires the reactor-test dependency (not included in the starter)
    void streamingChatStepVerifier() {
        Flux<String> output = chatClient.prompt()
                .user("Why is the sky blue?")
                .stream()
                .content();

        output.as(StepVerifier::create)
                .expectSubscription()
                .thenConsumeWhile(s -> true, System.out::println)
                .verifyComplete();
    }

    @Test
    void actorFilmsTest() {
        ActorFilms actorFilms = chatClient.prompt()
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);
        assertNotNull(actorFilms);
        System.out.println("Actor: " + actorFilms.actor());
        actorFilms.movies().forEach(System.out::println);
        
        String fullAnswer = "Actor: " + actorFilms.actor() + "\nMovies: " + String.join(", ", actorFilms.movies());
        double score = evaluateAnswer("Generate the filmography for a random actor.", fullAnswer);
    }

    @Test
    void listOfActorFilms() {
        List<ActorFilms> actorFilms = chatClient.prompt()
                .user("""
                        Generate the filmography of 5 movies
                        for Tom Hanks and Bill Murray.""")
                .call()
                .entity(new ParameterizedTypeReference<>() {
                });
        assertNotNull(actorFilms);
        actorFilms.forEach(actorFilm -> {
            System.out.println("Actor: " + actorFilm.actor());
            actorFilm.movies().forEach(System.out::println);
        });
    }

    @Test
    void promptTemplate() {
        String answer = chatClient.prompt()
                .user(u -> u
                        .text("""
                                Tell me the names of 5 movies
                                whose soundtrack was composed by {composer}""")
                        .param("composer", "John Williams"))
                .call()
                .content();
        System.out.println(answer);
    }

    @Test
    void promptTemplateFromResource() {
        String answer = chatClient.prompt()
                .user(u -> u
                        .text(promptTemplate)
                        .param("number", "10")
                        .param("composer", "Michael Giacchino"))
                .call()
                .content();
        System.out.println(answer);
    }

    @Test
    void requestsAreStateless() {
        // Use default memory advisor, then set ChatMemory.CONVERSATION_ID on each call
//        ChatClient chatClient = ChatClient.builder(model)
//                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
//                .build();
//
        // Or add the chat memory advisor to each request
        String conversationId = "inigo-demo";
        System.out.println("Initial query:");
        String answer1 = chatClient.prompt()
//                .advisors(a -> a
//                        .advisors(MessageChatMemoryAdvisor.builder(memory).build())
//                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(u -> u.text("""
                        My name is Inigo Montoya.
                        I am a fencing instructor from Florin."""))
                .call()
                .content();
        System.out.println(answer1);

        System.out.println("Second query:");
        String answer2 = chatClient.prompt()
//                .advisors(a -> a
//                        .advisors(MessageChatMemoryAdvisor.builder(memory).build())
//                        .param(ChatMemory.CONVERSATION_ID, conversationId))
                .user(u -> u.text("Who am I?"))
                .call()
                .content();
        System.out.println(answer2);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_MULTIMODAL_TESTS", matches = "true")
    void localVisionTest() {
        String response = chatClient.prompt()
                .user(u -> u.text("What do you see on this picture?")
                        .media(MimeTypeUtils.IMAGE_PNG, imageResource))
                .call()
                .content();
        System.out.println(response);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_MULTIMODAL_TESTS", matches = "true")
    void remoteVisionTest() {
        String imageUrl = "https://upload.wikimedia.org/wikipedia/commons/9/9a/Deelerwoud%2C_09-05-2024_%28actm.%29_04.jpg";
        String response = chatClient.prompt()
                .user(u -> {
                    try {
                        u.text("What do you see on this picture?")
                                .media(MimeTypeUtils.IMAGE_JPEG, URI.create(imageUrl).toURL());
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                })
                .call()
                .content();
        System.out.println(response);
    }

    @Test
    @EnabledIfEnvironmentVariable(named = "RUN_MULTIMODAL_TESTS", matches = "true")
    void imageGenerator(@Autowired OpenAiImageModel imageModel) {
        String prompt = """
                A warrior cat rides a dragon into battle""";

        System.out.println(imageModel.call(new ImagePrompt(prompt)));
    }

    @Test
    //@EnabledIfEnvironmentVariable(named = "RUN_MULTIMODAL_TESTS", matches = "true")
    void imageGeneratorBase64(@Autowired OpenAiImageModel imageModel) throws IOException {
        String prompt = """
                A warrior cat rides a dragon into battle""";

        // Note: with gpt-image-2, the response is returned as a base64-encoded string
        ImageResponse response = imageModel.call(
                new ImagePrompt(prompt,
                        OpenAiImageOptions.builder()
                                .model("gpt-image-2")
                                .build())
        );

        Image image = response.getResult().getOutput();
        assertNotNull(image);

        // Decode the base64 to bytes
        byte[] imageBytes = Base64.getDecoder().decode(image.getB64Json());

        // Write to file (e.g., PNG)
        Path outputPath = Path.of("build", "generated-images", "output_image.png");
        Files.createDirectories(outputPath.getParent());
        Files.write(outputPath, imageBytes);

        System.out.println("Image saved as " + outputPath);
    }

    @Test
    void useDateTimeTools() {
        String response = chatClient.prompt()
                .user("What day is tomorrow?")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(response);

        String alarmTime = chatClient.prompt()
                .user("Set an alarm for ten minutes from now")
                .tools(new DateTimeTools())
                .call()
                .content();
        System.out.println(alarmTime);
    }

    @Test
    void structuredOutputWithValidation() {
        // New in Spring AI 2.0: validates the JSON response against the target
        // schema and asks the model to repair it, up to maxRepeatAttempts times
        var validationAdvisor = StructuredOutputValidationAdvisor.builder()
                .outputType(ActorFilms.class)
                .maxRepeatAttempts(2)
                .build();

        ActorFilms actorFilms = chatClient.prompt()
                .advisors(validationAdvisor)
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);
        assertNotNull(actorFilms);
        System.out.println("Actor: " + actorFilms.actor());
        actorFilms.movies().forEach(System.out::println);
    }

    @Test
    void toolSearchAcrossManyTools() {
        // New in Spring AI 2.0: instead of sending every tool definition with
        // every request, the model gets a single search tool and pulls in only
        // the tools relevant to the query -- important once you have dozens of
        // tools (or several MCP servers) registered
        var toolSearchAdvisor = ToolSearchToolCallingAdvisor.builder()
                .toolIndex(new RegexToolIndex())
                .maxResults(3)
                .build();

        // The advisor keeps a per-session tool index, so it needs a
        // conversation id in the advisor context
        String response = chatClient.prompt()
                .advisors(a -> a.advisors(toolSearchAdvisor)
                        .param(ChatMemory.CONVERSATION_ID, "tool-search-demo"))
                .tools(new DateTimeTools(), new CalculatorService())
                .user("What is the square root of 1764?")
                .call()
                .content();
        System.out.println(response);
        assertTrue(response.contains("42"), "Expected sqrt(1764) = 42 in: " + response);
    }

}
