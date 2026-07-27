package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.web.client.RestClient;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Same lab progression as OpenAiTests, but against a local Ollama model.
 * No API key needed -- install Ollama (https://ollama.com), pull a model
 * (e.g. `ollama pull gemma4`), and set OLLAMA_MODEL if you want something
 * other than the default in application.properties.
 *
 * Note: the vision and tool-calling tests require a model that supports
 * those capabilities.
 */
@SuppressWarnings("unused")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "RUN_OLLAMA_TESTS", matches = "true")
class OllamaTests {

    @Value("classpath:movie_prompt.st")
    private Resource promptTemplate;

    @Value("classpath:bowl_of_fruit.png")
    private Resource imageResource;

    @Autowired
    private OllamaChatModel model;

    @Autowired
    private ChatMemory memory;

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        // Use builder to add default advisors
        chatClient = ChatClient.builder(model)
//                .defaultAdvisors(
//                        new SimpleLoggerAdvisor(),
//                        MessageChatMemoryAdvisor.builder(memory).build())
                .build();

        // Use create for defaults
        chatClient = ChatClient.create(model);
    }

    @Test
    void simpleQuery() {
        String response = chatClient.prompt()
                .advisors(new SimpleLoggerAdvisor()) // add advisor to existing chat client
                .user("Why is the sky blue?")
                .call()
                .content();
        System.out.println(response);
    }

    @Test
    void simpleQueryRespondLikeAPirate() {
        String response = chatClient.prompt()
                .system("You are a helpful assistant that responds like a pirate.")
                .user("Why is the sky blue?")
                .call()
                .content();
        System.out.println(response);
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
    void localVisionTest() {
        String response = chatClient.prompt()
                .user(u -> u.text("What do you see on this picture?")
                        .media(MimeTypeUtils.IMAGE_PNG, imageResource))
                .call()
                .content();
        System.out.println(response);
    }

    @Test
    void remoteVisionTest() {
        // Ollama's API accepts image bytes only (no URL pass-through like
        // OpenAI), so download the image first
        String imageUrl = "https://raw.githubusercontent.com/kousen/Spring_AI_Training_Course/main/public/images/ai-vision-dog.jpg";
        byte[] imageBytes = RestClient.create().get()
                .uri(imageUrl)
                .retrieve()
                .body(byte[].class);

        String response = chatClient.prompt()
                .user(u -> u.text("What do you see on this picture?")
                        .media(MimeTypeUtils.IMAGE_JPEG, new ByteArrayResource(imageBytes)))
                .call()
                .content();
        System.out.println(response);
    }

    // Note: Ollama chat models do not generate images, so imageGenerator and imageGeneratorBase64 tests are omitted

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

}
