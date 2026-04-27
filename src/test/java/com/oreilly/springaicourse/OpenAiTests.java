package com.oreilly.springaicourse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.audio.tts.TextToSpeechPrompt;
import org.springframework.ai.audio.tts.TextToSpeechResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.image.Image;
import org.springframework.ai.image.ImagePrompt;
import org.springframework.ai.image.ImageResponse;
import org.springframework.ai.openai.*;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import org.springframework.util.MimeTypeUtils;
import reactor.core.publisher.Flux;

import javax.swing.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SuppressWarnings("unused")
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
class OpenAiTests {

    @Value("classpath:prompts/movie_prompt.st")
    private Resource promptTemplate;

    @Value("classpath:issues.png")
    private Resource imageResource;

    @Autowired
    private OpenAiChatModel model;

    @Autowired
    private ChatMemory memory;

    @Value("classpath:audio/tftjs.mp3")
    private Resource sampleAudioResource;

    @Autowired
    JdbcChatMemoryRepository chatMemoryRepository;

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(10)
                .build();

        chatClient = ChatClient.builder(model)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .build();
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
        ChatClient chatClient = ChatClient.create(model);

        List<ActorFilms> actorFilms = chatClient.prompt()
                .user("Generate the filmography of 5 movies for Tom Hanks and Bill Murray.")
                .call()
                .entity(new ParameterizedTypeReference<>() {});

        assertNotNull(actorFilms);
        actorFilms.forEach(actorFilm -> {
            System.out.println("Actor: " + actorFilm.actor());
            actorFilm.movies().forEach(System.out::println);
        });
    }

    // === Lab 5: Prompt Templates ===

    @Test
    void promptTemplate() {
        String answer = chatClient.prompt()
                .user(u -> u
                        .text("Tell me the names of 5 movies whose soundtrack was composed by {composer}")
                        .param("composer", "Michael Giacchino"))
                .call()
                .content();

        System.out.println(answer);
    }

    @Test
    void promptTemplateFromResource() {
        ChatClient chatClient = ChatClient.create(model);

        String answer = chatClient.prompt()
                .user(u -> u
                        .text(promptTemplate)
                        .param("number", "10")
                        .param("composer", "Michael Giacchino"))
                .call()
                .content();

        System.out.println(answer);
    }

    // === Lab 6: Chat Memory ===

    @Test
    void defaultRequestsAreStateless() {
        System.out.println("Initial query:");
        String answer1 = chatClient.prompt()
                .user("My name is Inigo Montoya. You killed my father. Prepare to die.")
                .call()
                .content();
        System.out.println(answer1);

        System.out.println("Second query:");
        String answer2 = chatClient.prompt()
                .user("Who am I?")
                .call()
                .content();
        System.out.println(answer2);

        // Verify the model doesn't identify the user as Inigo Montoya
        assertFalse(answer2.toLowerCase().contains("inigo montoya"),
                "The model should not remember previous conversations without memory");
    }

    // === Lab 7: Vision Capabilities ===

    @Test
    void localVisionTest() {
        String response = chatClient.prompt()
                .user(u -> u.text(
                                """
                                Should we fix the issues identified
                                by IntelliJ IDEA shown in this image?
                                """
                        )
                        .media(MimeTypeUtils.IMAGE_PNG, imageResource))
                .call()
                .content();

        System.out.println(response);
    }

    @Test
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

    // === Lab 8: Image Generation ===

    @Test
    void imageGenerator(@Autowired OpenAiImageModel imageModel) throws IOException {
        String prompt = """
                A highly detailed, photorealistic cinematic scene of an anthropomorphic warrior cat riding a विशाल dragon into battle at dawn. The cat has thick, battle-worn fur (charcoal gray with subtle tabby markings), piercing amber eyes, and a scar across one cheek. He wears intricately crafted medieval armor fitted to his feline form—brushed steel with engraved runes, leather straps, and a flowing crimson cape whipping in the wind.
                
                                                The dragon is enormous and lifelike, with textured scales in deep emerald and obsidian tones, glowing molten veins beneath the surface, and wide, powerful wings mid-flap. Its eyes burn with intelligence and fury, and faint smoke curls from its nostrils.
                
                                                They soar low over a chaotic battlefield filled with armored soldiers, banners, and distant explosions of fire and dust. The lighting is dramatic: golden sunrise light cutting through heavy smoke and clouds, casting long shadows and illuminating particles in the air. Motion blur subtly enhances the sense of speed and action.
                
                                                Shot in ultra-realistic style, 8K resolution, shallow depth of field, cinematic composition, high dynamic range (HDR), realistic textures (fur, metal, scales), volumetric lighting, and physically accurate shadows. Camera angle is slightly below and behind, emphasizing scale and heroism.""";

        // Note: when using the "gpt-image-2" model,
        // the response is automatically base64-encoded and you should not
        // specify responseFormat
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
        Files.write(Path.of("src/main/resources","output_image.png"), imageBytes);
        System.out.println("Image saved as output_image.png in src/main/resources");

    }

    @Test
    void imageGeneratorBase64(@Autowired OpenAiImageModel imageModel) {
        // TODO: Generate image and save as base64-encoded file
        // Use gpt-image-1 model for base64 response
        // Decode and save to src/main/resources/output_image.png
    }

    @Test
    void textToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) {
        String text = """
         Olá! Uma boa tarde de Portugal!""";

        OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
                .voice(OpenAiAudioApi.SpeechRequest.Voice.FABLE)
                .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
                .speed(1.0)
                .build();

        TextToSpeechPrompt prompt = new TextToSpeechPrompt(text, options);
        TextToSpeechResponse response = speechModel.call(prompt);
        assertNotNull(response);

        // Optionally save to file for verification
        try {
            Files.write(Path.of("generated_audio.mp3"), response.getResult().getOutput());
            System.out.println("Audio file generated and saved as 'generated_audio.mp3'");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void speechToText(@Autowired OpenAiAudioTranscriptionModel transcriptionModel) {

        // Optional configuration
        OpenAiAudioTranscriptionOptions options = OpenAiAudioTranscriptionOptions.builder()
                .language("en")
                .prompt("Transcribe this audio file.")
                .temperature(0.0f)
                .responseFormat(OpenAiAudioApi.TranscriptResponseFormat.TEXT)
                .build();

        AudioTranscriptionPrompt prompt = new AudioTranscriptionPrompt(sampleAudioResource, options);
        AudioTranscriptionResponse response = transcriptionModel.call(prompt);
        assertNotNull(response);
        System.out.println("Transcription: " + response.getResult().getOutput());
    }

    // === Lab 10: AI Tools ===

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
