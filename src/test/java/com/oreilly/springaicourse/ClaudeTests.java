package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.anthropic.AnthropicChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unused")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "ANTHROPIC_API_KEY", matches = ".+")
class ClaudeTests {

    @Value("classpath:movie_prompt.st")
    private Resource promptTemplate;

    @Value("classpath:bowl_of_fruit.png")
    private Resource imageResource;

    @Autowired
    private AnthropicChatModel model;

    @Autowired
    private ChatMemory memory;

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        // TODO: Initialize ChatClient using ChatClient.create(model)
        // For more advanced features, use ChatClient.builder(model) with advisors
    }

    // === Lab 1: Basic Chat Interactions ===

    @Test
    void simpleQuery() {
        // TODO: Create a simple chat interaction using Claude
        // Use chatClient.prompt().user("Why is the sky blue?").call().content()
        // Print the response
    }

    @Test
    void simpleQueryRespondLikeAPirate() {
        // TODO: Add a system message to make the AI respond like a pirate
        // Use .system("You are a helpful assistant that responds like a pirate.")
    }

    @Test
    void simpleQueryWithChatResponse() {
        // TODO: Get the full ChatResponse instead of just content
        // Use .call().chatResponse() to access metadata like model and usage info
        // Print model, usage, and response text
    }

    // === Lab 3: Streaming Responses ===

    @Test
    void streamingChatCountDownLatch() throws InterruptedException {
        // TODO: Implement streaming chat using CountDownLatch
        // Use .stream().content() to get a Flux<String>
        // Subscribe with error handling and completion signaling
    }

    @Test
    void streamingChatDoOnNext() {
        // TODO: Implement streaming using reactive operators
        // Use doOnNext, doOnError, doOnComplete, and blockLast()
    }

    @Test // Note: Requires the reactor-test dependency (not included in the starter)
    void streamingChatStepVerifier() {
        // TODO: Implement streaming using StepVerifier for deterministic testing
        // Use StepVerifier.create() for robust reactive stream testing
        // This is the recommended approach for production code
    }

    // === Lab 4: Structured Data Extraction ===

    @Test
    void actorFilmsTest() {
        // TODO: Extract structured data as ActorFilms record
        // Use .entity(ActorFilms.class) to get structured response
        // Print actor name and movies
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
        // First query: "My name is Inigo Montoya. I am a fencing instructor from Florin."
        // Second query: "Who am I?"
        // When enabling memory, also set ChatMemory.CONVERSATION_ID on each call
    }

    // === Lab 7: Vision Capabilities ===

    @Test
    void localVisionTest() {
        // TODO: Analyze a local image file with Claude
        // Use .media(MimeTypeUtils.IMAGE_PNG, imageResource)
        // Ask "What do you see on this picture?"
    }

    @Test
    void remoteVisionTest() {
        // TODO: Analyze a remote image from URL with Claude
        // Use URI.create(imageUrl).toURL() with proper exception handling
    }

    // Note: Claude API does not support image generation, so Lab 8 tests are omitted

    // === Lab 10: AI Tools ===

    @Test
    void useDateTimeTools() {
        // TODO: Use DateTimeTools for time-related queries with Claude
        // Ask "What day is tomorrow?" and "Set an alarm for ten minutes from now"
        // Use .tools(new DateTimeTools())
    }

}
