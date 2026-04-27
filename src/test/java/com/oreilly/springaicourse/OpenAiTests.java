package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiImageModel;
import org.springframework.ai.openai.OpenAiImageOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    @BeforeEach
    void setUp() {
        // TODO: Initialize ChatClient using ChatClient.create(model)
        // For more advanced features, use ChatClient.builder(model) with advisors

        chatClient = ChatClient.create(model);
    }

    // === Lab 1: Basic Chat Interactions ===

    @Test
    void simpleQuery() {
        // TODO: Create a simple chat interaction
        // Use chatClient.prompt().user("Why is the sky blue?").call().content()
        // Print the response

        chatClient = ChatClient.builder(model)
                .defaultOptions(ChatOptions.builder()
                        .temperature(1.0)
                        .build())
                .build();
        ChatResponse chatResponse = chatClient.prompt()
                .user("Why is the sky blue?")
                .call()
                .chatResponse();
        assertNotNull(chatResponse);
        ChatResponseMetadata metadata = chatResponse.getMetadata();
        System.out.println(metadata.getModel());
        System.out.println(metadata.getUsage());
        System.out.println(chatResponse.getResult().getOutput().getText());
    }

    @Test
    void simpleQueryRespondLikeAPirate() {
        // TODO: Add a system message to make the AI respond like a pirate
        // Use .system("You are a helpful assistant that responds like a pirate.")
        String response = chatClient.prompt()
                .system("You are a helpful assistant that responds like a pirate.")
                .user("Why is the sky blue?")
                .call()
                .content();
        System.out.println(response);
    }

    @Test
    void simpleQueryWithChatResponse() {
        // TODO: Get the full ChatResponse instead of just content
        // Use .call().chatResponse() to access metadata like model and usage info
        // Print model, usage, and response text

        ChatClient chatClient = ChatClient.create(model);
        ChatResponse chatResponse = chatClient.prompt()
                .system("You are a helpful assistant that responds like a pirate.")
                .user("Why is the sky blue?")
                .call()
                .chatResponse();

        assertNotNull(chatResponse);
        ChatResponseMetadata metadata = chatResponse.getMetadata();
        System.out.println(metadata.getModel());
        System.out.println(metadata.getUsage());
        System.out.println(chatResponse.getResult().getOutput().getText());
    }

    @Test
    void loggingAdvisorTest() {
        // Create a chat client from the model with logging advisor
        ChatClient chatClient = ChatClient.builder(model)
                .defaultAdvisors(new SimpleLoggerAdvisor())
                .build();

        // Send a prompt and get the response
        String response = chatClient.prompt()
                .user("Explain the concept of recursion in programming")
                .call()
                .content();

        System.out.println("Response: " + response);
    }

    // === Lab 3: Streaming Responses ===

    @Test
    void streamingChatCountDownLatch() throws InterruptedException {
        ChatClient chatClient = ChatClient.create(model);

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
        ChatClient chatClient = ChatClient.create(model);

        Flux<String> output = chatClient.prompt()
                .user("Why is the sky blue?")
                .stream()
                .content();

        output.doOnNext(System.out::println)
                .doOnCancel(() -> System.out.println("Cancelled"))
                .doOnComplete(() -> System.out.println("Completed"))
                .doOnError(e -> System.out.println("Error: " + e.getMessage()))
                .blockLast();
    }

    // === Lab 4: Structured Data Extraction ===

    @Test
    void actorFilmsTest() {
        ActorFilms actorFilms = chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor())
                .user("Generate the filmography for a random actor.")
                .call()
                .entity(ActorFilms.class);

        assertNotNull(actorFilms);
        System.out.println("Actor: " + actorFilms.actor());
        actorFilms.movies().forEach(System.out::println);
    }

    @Test
    void listOfActorFilms() {
        // TODO: Extract a list of ActorFilms using ParameterizedTypeReference
        // Request filmography for multiple actors (Tom Hanks and Bill Murray)
    }

    // === Lab 5: Prompt Templates ===

    @Test
    void promptTemplate() {
        // TODO: Use inline prompt template with parameters
        // Template: "Tell me the names of 5 movies whose soundtrack was composed by {composer}"
        // Use .param("composer", "John Williams")
    }

    @Test
    void promptTemplateFromResource() {
        // TODO: Load prompt template from movie_prompt.st resource file
        // Use .text(promptTemplate) and parameters for number and composer
    }

    // === Lab 6: Chat Memory ===

    @Test
    void requestsAreStateless() {
        // TODO: Demonstrate stateless requests vs memory-enabled chat
        // First query: "My name is Inigo Montoya. You killed my father. Prepare to die."
        // Second query: "Who am I?"
        // Uncomment MessageChatMemoryAdvisor lines to enable memory
    }

    // === Lab 7: Vision Capabilities ===

    @Test
    void localVisionTest() {
        // TODO: Analyze a local image file
        // Use .media(MimeTypeUtils.IMAGE_PNG, imageResource)
        // Ask "What do you see on this picture?"
    }

    @Test
    void remoteVisionTest() {
        // TODO: Analyze a remote image from URL
        // Use URI.create(imageUrl).toURL() with proper exception handling
    }

    // === Lab 8: Image Generation ===

    @Test
    void imageGenerator(@Autowired OpenAiImageModel imageModel) {
        // TODO: Generate an image using DALL-E
        // Create ImagePrompt with description
        // Use imageModel.call() to generate
    }

    @Test
    void imageGeneratorBase64(@Autowired OpenAiImageModel imageModel) throws IOException {
        // TODO: Generate image and save as base64-encoded file
        // Use gpt-image-1 model for base64 response
        // Decode and save to src/main/resources/output_image.png
    }

    // === Lab 10: AI Tools ===

    @Test
    void useDateTimeTools() {
        // TODO: Use DateTimeTools for time-related queries
        // Ask "What day is tomorrow?" and "Set an alarm for ten minutes from now"
        // Use .tools(new DateTimeTools())
    }

}
