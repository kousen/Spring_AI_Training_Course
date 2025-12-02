# Spring AI Course Labs

This series of labs will guide you through building a Spring AI application that uses various capabilities of large language models via the Spring AI abstraction layer. By the end of these exercises, you'll have hands-on experience with text generation, structured data extraction, prompt templates, chat memory, vision capabilities, and more.

> **Note:** This project uses Spring Boot 3.5.8 and Spring AI 1.1.0. Spring AI 1.1.0 includes significant API changes, including using builder patterns for constructing advisors like `MessageChatMemoryAdvisor`, and updated vector store implementations.

## Table of Contents

- [Setup](#setup)
- [Lab 1: Basic Chat Interactions](#lab-1-basic-chat-interactions)
- [Lab 2: Request and Response Logging](#lab-2-request-and-response-logging)
- [Lab 3: Streaming Responses](#lab-3-streaming-responses)
- [Lab 4: Structured Data Extraction](#lab-4-structured-data-extraction)
- [Lab 5: Prompt Templates](#lab-5-prompt-templates)
- [Lab 6: Chat Memory](#lab-6-chat-memory)
- [Lab 7: Vision Capabilities](#lab-7-vision-capabilities)
- [Lab 8: Image Generation](#lab-8-image-generation)
- [Lab 9: Audio Capabilities](#lab-9-audio-capabilities)
- [Lab 10: AI Tools](#lab-10-ai-tools)
- [Lab 11: Refactoring for Production](#lab-11-refactoring-for-production)
- [Lab 12: Retrieval-Augmented Generation (RAG)](#lab-12-retrieval-augmented-generation-rag)
- [Lab 13: Redis Vector Store for RAG](#lab-13-redis-vector-store-for-rag)
- [Lab 14: Model Context Protocol (MCP) - Client](#lab-14-model-context-protocol-mcp---client)
- [Lab 15: Model Context Protocol (MCP) - Server](#lab-15-model-context-protocol-mcp---server)
- [Conclusion](#conclusion)

## Setup

1. Make sure you have the following prerequisites:
   - Java 17+
   - An IDE (IntelliJ IDEA, Eclipse, VS Code)
   - API keys for OpenAI and/or Anthropic (Claude)

2. Set the required environment variables:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key
   export ANTHROPIC_API_KEY=your_anthropic_api_key  # Optional, for Claude exercises
   ```

3. **Model Configuration Note**: This course uses `gpt-5-nano` (OpenAI) and `claude-opus-4-1` (Anthropic). These models have specific requirements:
   - `gpt-5-nano` only supports `temperature=1.0` (the default value)
   - When using `ChatClient.builder()`, explicitly set temperature if needed:
     ```java
     ChatClient chatClient = ChatClient.builder(model)
         .defaultOptions(ChatOptions.builder().temperature(1.0).build())
         .build();
     ```

4. Check that the project builds successfully:
   ```bash
   ./gradlew build
   ```

## Lab 1: Basic Chat Interactions

### 1.1 A Simple Query

In the test class (`OpenAiTests.java`), autowire in an instance of OpenAI's chat model:

```java
@Autowired
private OpenAiChatModel model;
```

Create a test method that sends a simple query to the OpenAI model using Spring AI's ChatClient:

```java
@Test
void simpleQuery() {
    // Create a chat client from the model
    ChatClient chatClient = ChatClient.create(model);

    // Send a prompt and get the response
    String response = chatClient.prompt()
            .user("Why is the sky blue?")
            .call()
            .content();

    System.out.println(response);
}
```

### 1.2 System Prompt

Modify the previous test to include a system prompt that changes the model's behavior:

```java
@Test
void simpleQueryRespondLikeAPirate() {
    ChatClient chatClient = ChatClient.create(model);

    String response = chatClient.prompt()
            .system("You are a helpful assistant that responds like a pirate.")
            .user("Why is the sky blue?")
            .call()
            .content();

    System.out.println(response);
}
```

### 1.3 Accessing Response Metadata

Create a test that retrieves and displays the full `ChatResponse` object:

```java
@Test
void simpleQueryWithChatResponse() {
    ChatClient chatClient = ChatClient.create(model);

    ChatResponse response = chatClient.prompt()
            .user("Why is the sky blue?")
            .call()
            .chatResponse();

    assertNotNull(response);
    System.out.println("Model: " + response.getMetadata().getModel());
    System.out.println("Usage: " + response.getMetadata().getUsage());
    System.out.println("Response: " + response.getResult().getOutput().getText());
}
```

Note how the metadata provides useful information about the model and the token usage.

[↑ Back to table of contents](#table-of-contents)

## Lab 2: Request and Response Logging

When working with AI models, it's often useful to see exactly what prompts are being sent to the model and what responses are being received, especially for debugging. Spring AI includes a `SimpleLoggerAdvisor` that logs detailed information about each interaction.

### 2.1 Configure Logging in application.properties

First, enable debug logging for the advisor package in your `application.properties`. This is **required** for the SimpleLoggerAdvisor to show its output:

```properties
# Enable debug logging for AI advisors (MUST be set to DEBUG level)
logging.level.org.springframework.ai.chat.client.advisor=DEBUG
```

This setting ensures that the full details of prompts and responses will be logged. Without this configuration (or if set to INFO level), you won't see the detailed logs from SimpleLoggerAdvisor.

### 2.2 Using SimpleLoggerAdvisor

Create a test that adds the `SimpleLoggerAdvisor` to see request and response details:

```java
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
```

When you run this test, you'll see detailed logs that include:
- The full system and user messages being sent
- The model's complete response
- Timing information (how long the request took)

### 2.3 Adding the Advisor to Individual Requests

Instead of using the builder to set default advisors, you can add the advisor to specific requests:

```java
@Test
void individualRequestLogging() {
    ChatClient chatClient = ChatClient.create(model);

    String response = chatClient.prompt()
            .advisors(new SimpleLoggerAdvisor())
            .user("What is the capital of France?")
            .call()
            .content();

    System.out.println("Response: " + response);
}
```

This approach is useful when you only want logging for specific requests rather than all interactions.

### 2.4 Combining Multiple Advisors

The real power of advisors comes when you combine them (we'll explore others like the `MessageChatMemoryAdvisor` in later labs):

```java
@Test
void multipleAdvisors() {
    ChatClient chatClient = ChatClient.builder(model)
            .defaultAdvisors(
                new SimpleLoggerAdvisor()
                // Other advisors can be added here
            )
            .build();

    String response = chatClient.prompt()
            .user("Suggest three names for a pet turtle")
            .call()
            .content();

    System.out.println("Response: " + response);
}
```

The advisors are applied in the order they are specified, allowing you to build powerful processing pipelines.

[↑ Back to table of contents](#table-of-contents)

## Lab 3: Streaming Responses

### 3.1 Streaming with CountDownLatch

Create a test that streams the response. While the code will work,
the challenge in a JUnit test is to keep the test from exiting
before the asynchronous response returns. One way to do that is 
using a CountDownLatch:

```java
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
```

Note how the three-argument version of the `subscribe` method takes
lambda expressions for the individual callbacks. The first one is for the
normal response, the second one is for errors, and the third one
is for completion. The `subscribe` method returns a `Disposable` object
that can be used to cancel the subscription if needed. The `CountDownLatch`
is used to handle the asynchronous response.

### 3.2 Streaming with Reactor Operators

A simpler way to handle the same issue is to use Reactor's operators
to process the stream. This way, you can avoid using a `CountDownLatch`
and instead use the `doOnNext`, `doOnError`, and `doOnComplete` methods
to handle the response. This is a more idiomatic way to work with
Reactor streams and allows you to chain multiple operations together.

```java
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
```

[↑ Back to table of contents](#table-of-contents)

## Lab 4: Structured Data Extraction

### 4.1 Create the Data Class

Create a record to represent structured data:

```java
public record ActorFilms(String actor, List<String> movies) {}
```

### 4.2 Single Entity Extraction

Create a test that extracts a single entity:

```java
@Test
void actorFilmsTest() {
    ChatClient chatClient = ChatClient.create(model);
    
    ActorFilms actorFilms = chatClient.prompt()
            .user("Generate the filmography for a random actor.")
            .call()
            .entity(ActorFilms.class);
            
    assertNotNull(actorFilms);
    System.out.println("Actor: " + actorFilms.actor());
    actorFilms.movies().forEach(System.out::println);
}
```

### 4.3 Collection of Entities

The above approach works for a single instance of a class, even if that
class contains a collection. However, if you want to extract a collection,
you need to use a `ParameterizedTypeReference` to specify the type of the collection.

```java
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
```

[↑ Back to table of contents](#table-of-contents)

## Lab 5: Prompt Templates

### 5.1 Inline Template

Create a test using an inline prompt template:

```java
@Test
void promptTemplate() {
    ChatClient chatClient = ChatClient.create(model);
    
    String answer = chatClient.prompt()
            .user(u -> u
                    .text("Tell me the names of 5 movies whose soundtrack was composed by {composer}")
                    .param("composer", "John Williams"))
            .call()
            .content();
            
    System.out.println(answer);
}
```

The prompt template is simply a string that by default uses
`{name}` placeholders for parameters. You can use the `param` method
to set the values for these parameters. The `param` method can be called
multiple times to set multiple parameters. The `text` method is used
to set the text of the prompt.

### 5.2 Template from Resource

Spring AI includes its template engine called Spring Templates,
which allows you to create templates in a more structured way. You can
use this engine to create templates that are stored in files, making it
easier to manage and reuse them. The templates can be stored in the
`src/main/resources` directory, and you can use the `@Value` annotation
to inject the template into your code. The default file extension
for templates is `.st`, but you can use any extension you like.

First, create a template file at `src/main/resources/movie_prompt.st`:
```
Tell me the names of {number} movies whose soundtrack was composed by {composer}
```

Next, autowire the template resource into your test class using the `@Value` annotation:

```java
@Value("classpath:movie_prompt.st")
private Resource promptTemplate;
```

Then create a test that uses this template:

```java
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
```

If you need to use an alternative delimiter for the template variables,
other than `{}`, you can specify one. See the Spring AI documentation
for more details.

[↑ Back to table of contents](#table-of-contents)

## Lab 6: Chat Memory

### 6.1 Demonstrating Stateless Behavior

All requests to AI tools are stateless by default, meaning no
conversation history is retained between requests. This is useful
for one-off queries but can be limiting for conversational
interactions.

Create a test that demonstrates how requests are stateless by default:

```java
@Test
void defaultRequestsAreStateless() {
    ChatClient chatClient = ChatClient.create(model);

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
```

### 6.2 Adding Memory to Retain Conversation State

Use the `ChatMemory` abstraction to maintain the previous user and
assistant messages. Fortunately, you can autowire in a `ChatMemory` bean.

```java
@Autowired
private ChatMemory memory;
```

You'll also need to import the `MessageChatMemoryAdvisor`:

```java
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
```

Create a test that demonstrates how to make conversations stateful using ChatMemory:

```java
@Test
void requestsWithMemory() {
    ChatClient chatClient = ChatClient.create(model);

    System.out.println("Initial query with memory:");
    String answer1 = chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(memory).build())
            .user("My name is Inigo Montoya. You killed my father. Prepare to die.")
            .call()
            .content();
    System.out.println(answer1);

    System.out.println("Second query with memory:");
    String answer2 = chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(memory).build())
            .user("Who am I?")
            .call()
            .content();
    System.out.println(answer2);

    // Verify the model correctly identifies the user as Inigo Montoya
    assertTrue(answer2.toLowerCase().contains("inigo montoya"),
            "The model should remember the user's identity when using memory");
}
```

This example showed how to add chat memory to each 
request. However, you can also use the `ChatClient` builder
to set the memory advisor for all requests. This is useful
if you want to maintain the conversation state across multiple
requests without having to specify the memory advisor each time.

```java
ChatClient chatClient = ChatClient.builder(model)
        .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
        .build();
```

If you add that to the `setUp` method, you can remove the
`advisors` method from the individual requests.

```java
@BeforeEach
void setUp() {
    // Use builder to add default advisors
    chatClient = ChatClient.builder(model)
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
            .build();
}
```

To use that approach, be sure to add a `ChatClient` field
to the test class:

```java
private ChatClient chatClient;
```

[↑ Back to table of contents](#table-of-contents)

## Lab 7: Vision Capabilities

### 7.1 Local Image

First, make sure you have a test image in `src/main/resources/bowl_of_fruit.png`.

Then create a test that analyzes a local image:

```java
@Test
void localVisionTest() {
    ChatClient chatClient = ChatClient.create(model);
    
    String response = chatClient.prompt()
            .user(u -> u.text("What do you see on this picture?")
                    .media(MimeTypeUtils.IMAGE_PNG, imageResource))
            .call()
            .content();
            
    System.out.println(response);
}
```

### 7.2 Remote Image

Create a test that analyzes a remote image:

```java
@Test
void remoteVisionTest() {
    ChatClient chatClient = ChatClient.create(model);
    
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
```

This is a simple example. More commonly, you would ask an AI
to read text from an image, like a screenshot of an error message.

[↑ Back to table of contents](#table-of-contents)

## Lab 8: Image Generation

Create a test that generates an image. Note that you can
autowire in the `OpenAiImageModel` bean:

```java
@Test
void imageGenerator(@Autowired OpenAiImageModel imageModel) {
    String prompt = """
            A warrior cat rides a dragon into battle""";
    
    // Note: As of Spring AI 1.0.0, you must specify a model for image generation
    var imageOptions = OpenAiImageOptions.builder()
            .model("gpt-image-1")
            .build();
    
    var imagePrompt = new ImagePrompt(prompt, imageOptions);
    ImageResponse imageResponse = imageModel.call(imagePrompt);
    
    System.out.println(imageResponse);
}
```

The response object will contain an `Image` that includes a URL
to the generated image. You can use this URL to display the image
in a web application or save it to a file. The image is only 
available for a limited time, so be sure to download it
if you want to keep it.

If you use the new image model, "gpt-image-1", the 
response is returned as a base64-encoded string. Fortunately,
Java includes a built-in decoder that can be used to
extract the image and save it to a file.

```java
    @Test
void imageGeneratorBase64(@Autowired OpenAiImageModel imageModel) throws IOException {
   String prompt = """
           A warrior cat rides a dragon into battle""";

   // Note: With Spring AI 1.0.0, when using "gpt-image-1" model,
   // the response is automatically base64-encoded and you should not
   // specify responseFormat
   ImageResponse response = imageModel.call(
           new ImagePrompt(prompt,
                   OpenAiImageOptions.builder()
                           .model("gpt-image-1")
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
```

You can change the file name and format as needed. For DALL-E 3 model, you can still use `responseFormat` parameter with values like "url" or "b64_json".

[↑ Back to table of contents](#table-of-contents)

## Lab 9: Audio Capabilities

### 9.1 Text-to-Speech (TTS)

Create a test that generates speech from text. Note that Spring AI 1.1.0 introduced a new portable TTS API using `TextToSpeechPrompt` and `TextToSpeechResponse` from `org.springframework.ai.audio.tts`:

```java
@Test
void textToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) {
    String text = "Welcome to Spring AI, a powerful framework for integrating AI into your Spring applications.";

    OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
            .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
            .speed(1.0)  // Note: Changed from Float to Double in 1.1.0
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
```

### 9.2 Speech-to-Text (Transcription)

First, autowire in the `src/main/resources/audio/tftjs.mp3`:

```java
@Value("classpath:audio/tftjs.mp3")
private Resource sampleAudioResource;
```

Then create a test that transcribes speech to text:

```java
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
```

[↑ Back to table of contents](#table-of-contents)

## Lab 10: AI Tools

### 10.1 Create a Tool

Create a DateTimeTools class that the AI can use:

```java
class DateTimeTools {
    private final Logger logger = LoggerFactory.getLogger(DateTimeTools.class);

    @Tool(description = "Get the current date and time in the user's timezone")
    String getCurrentDateTime() {
        logger.info("Getting current date and time in the user's timezone");
        return LocalDateTime.now().atZone(LocaleContextHolder.getTimeZone().toZoneId()).toString();
    }

    @Tool(description = "Set a user alarm for the given time, provided in ISO-8601 format")
    void setAlarm(String time) {
        LocalDateTime alarmTime = LocalDateTime.parse(time, DateTimeFormatter.ISO_DATE_TIME);
        System.out.println("Alarm set for " + alarmTime);
    }
}
```

### 10.2 Use the Tools

Create a test that uses the annotated methods:

```java
@Test
void useDateTimeTools() {
    ChatClient chatClient = ChatClient.create(model);
    
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
```

[↑ Back to table of contents](#table-of-contents)

## Lab 11: Refactoring for Production

### 11.1 Create a Common Setup

Refactor your tests to use a common setup method:

```java
@BeforeEach
void setUp() {
    // Use builder to add default advisors if desired
    chatClient = ChatClient.builder(model)
            .defaultAdvisors(
                    MessageChatMemoryAdvisor.builder(memory).build(),
                    new SimpleLoggerAdvisor())
            .build();

    // Use create for defaults
    chatClient = ChatClient.create(model);
}
```

### 11.2 Add Service Classes

Create service classes for your application that use Spring AI under the hood.

For example, a FilmographyService:

```java
@Service
public class FilmographyService {
    private final ChatClient chatClient;
    
    public FilmographyService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }
    
    public List<ActorFilms> getFilmography(String... actors) {
        String actorList = String.join(" and ", actors);
        return chatClient.prompt()
                .user("Generate the filmography of 5 movies for " + actorList + ".")
                .call()
                .entity(new ParameterizedTypeReference<>() {});
    }
}
```

### 11.3 Create API Endpoints

Add a REST controller to expose your AI capabilities:

```java
@RestController
@RequestMapping("/api/films")
public class FilmographyController {
    private final FilmographyService service;
    
    public FilmographyController(FilmographyService service) {
        this.service = service;
    }
    
    @GetMapping("/{actors}")
    public List<ActorFilms> getFilmography(@PathVariable String actors) {
        return service.getFilmography(actors.split(","));
    }
}
```

[↑ Back to table of contents](#table-of-contents)

## Lab 12: Retrieval-Augmented Generation (RAG)

In this lab, you'll build a Retrieval-Augmented Generation (RAG) system using Spring AI's document readers and vector store capabilities. RAG enhances AI responses by retrieving relevant information from a knowledge base before generating answers.

### 12.1 Adding Required Dependencies

First, add the necessary dependencies to your build.gradle.kts:

```kotlin
dependencies {
    // Existing dependencies...

    // Vector store for RAG implementation
    implementation("org.springframework.ai:spring-ai-advisors-vector-store")

    // Document readers for different content types
    implementation("org.springframework.ai:spring-ai-jsoup-document-reader") // For HTML/web content
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")   // Optional: For PDF documents
}
```

### 12.2 Setting up the Configuration

Create a configuration class that will manage the vector store and document loading:

```java
@Configuration
public class AppConfig {
    // URLs for our knowledge base
    private static final String SPRING_URL = "https://en.wikipedia.org/wiki/Spring_Framework";
    private static final String SPRING_BOOT_URL = "https://en.wikipedia.org/wiki/Spring_Boot";

    // Use the default token-based text splitter
    private final TextSplitter splitter = new TokenTextSplitter();

    @Bean
    @Profile("rag") // Only activate when the 'rag' profile is enabled
    ApplicationRunner loadVectorStore(VectorStore vectorStore) {
        return args -> List.of(SPRING_URL, SPRING_BOOT_URL).forEach(url -> {
            // Fetch HTML content using Spring AI's JsoupDocumentReader
            List<Document> documents = new JsoupDocumentReader(url).get();
            System.out.println("Fetched " + documents.size() + " documents from " + url);

            // Split the documents into chunks for better retrieval
            List<Document> chunks = splitter.apply(documents);

            // Add the chunks to the vector store
            vectorStore.add(chunks);
        });
    }

    @Bean
    VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
```

Make sure your application.properties file is configured properly:

```properties
# Use the smaller embedding model for better performance
spring.ai.openai.embedding.options.model=text-embedding-3-small

# Reduce logging levels for cleaner output
logging.level.org.springframework.ai=info
logging.level.org.springframework.ai.chat.client.advisor=info
```

### 12.3 Creating the RAG Service

Create a service class that handles queries against your knowledge base:

```java
@Service
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Autowired
    public RAGService(
            OpenAiChatModel chatModel,
            VectorStore vectorStore) {
        this.chatClient = ChatClient.create(chatModel);
        this.vectorStore = vectorStore;
    }

    public String query(String question) {
        // Create a QuestionAnswerAdvisor with the vectorStore
        QuestionAnswerAdvisor advisor = new QuestionAnswerAdvisor(question);

        // Use the advisor to handle the RAG workflow
        return chatClient.prompt()
                .advisors(advisor)
                .system(instructionPrompt)
                .user(question)
                .call()
                .content();
    }
}
```

### 12.4 Testing the RAG System

Create an integration test to verify your RAG system works correctly:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("rag") // Enable the RAG profile for this test
public class RAGTests {

    @Autowired
    private RAGService ragService;

    @Test
    void retrievalAugmentedGeneration() {
        // Query about Spring (should return relevant info)
        String question = "What is the Spring Framework and what are its key features?";
        String response = ragService.query(question);

        System.out.println("RAG Response about Spring:");
        System.out.println(response);

        // Assertions for Spring Framework query
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    @Test
    void outOfScopeQuery() {
        // Query about something not in our knowledge base
        String outOfScopeQuestion = "How do I implement GraphQL in Spring?";
        String outOfScopeResponse = ragService.query(outOfScopeQuestion);

        System.out.println("\nOut of scope RAG Response:");
        System.out.println(outOfScopeResponse);

        // Assertions for out-of-scope query
        assertNotNull(outOfScopeResponse);
        assertTrue(outOfScopeResponse.contains("don't have enough information") ||
                outOfScopeResponse.contains("not enough information") ||
                outOfScopeResponse.contains("cannot provide information"),
                "Should indicate lack of information for out-of-scope questions");
    }
}
```

### 12.5 Running with the RAG Profile

To run your application with RAG enabled, set the active profile:

```bash
# Command line
./gradlew bootRun --args='--spring.profiles.active=rag'

# In IntelliJ IDEA
# Edit Run Configuration -> Program arguments: --spring.profiles.active=rag
```

By using the profile approach, you ensure that the RAG system only loads its knowledge base when explicitly enabled, preventing unnecessary processing during regular application use or other tests.

### 12.5 Using a Persistent Vector Store

For long-term persistence and better performance with large documents, you can consider using a persistent vector store like Chroma or PostgreSQL.

Spring AI provides integrations with several vector store options, including:

1. **SimpleVectorStore** (default, in-memory): Easiest to set up but doesn't persist data between application restarts
2. **PostgresVectorStore**: Uses PostgreSQL with the pgvector extension
3. **ChromaVectorStore**: Uses the Chroma vector database
4. **PineconeVectorStore**: Uses Pinecone, a managed vector database service
5. **WeaviateVectorStore**: Uses Weaviate, a vector search engine

Each of these options has different setup requirements and performance characteristics.

For this lab, we're using the SimpleVectorStore for ease of setup, but in production environments, a persistent vector store would typically be preferred.

### 12.6 Testing RAG Response Quality with RelevancyEvaluator

Spring AI provides a unique feature called `RelevancyEvaluator` that uses AI to evaluate the quality and relevance of RAG responses. This is particularly valuable for validating that your RAG system is working correctly and producing contextually appropriate responses.

#### 12.6.1 Enhance RAGService for Testing

First, modify your RAGService to provide access to the full ChatResponse object for testing:

```java
@Service
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemory memory;

    @Autowired
    public RAGService(
            OpenAiChatModel chatModel,
            VectorStore vectorStore, ChatMemory memory) {
        this.chatClient = ChatClient.create(chatModel);
        this.vectorStore = vectorStore;
        this.memory = memory;
    }

    public String query(String question) {
        return queryWithResponse(question).getResult().getOutput().getText();
    }
    
    /**
     * Query the RAG system and return the full ChatResponse with metadata.
     * Useful for testing and accessing document context.
     */
    public ChatResponse queryWithResponse(String question) {
        // Create a QuestionAnswerAdvisor with the vectorStore
        var questionAnswerAdvisor = new QuestionAnswerAdvisor(vectorStore);

        // Good to use chat memory when doing RAG
        var chatMemoryAdvisor = MessageChatMemoryAdvisor.builder(memory).build();

        // Use the advisor to handle the RAG workflow
        return chatClient.prompt()
                .advisors(questionAnswerAdvisor, chatMemoryAdvisor)
                .user(question)
                .call()
                .chatResponse();
    }
}
```

#### 12.6.2 Update RAG Tests with RelevancyEvaluator

Enhance your RAG tests to use Spring AI's semantic evaluation capabilities:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"rag","redis"})
public class RAGTests {

    @Autowired
    private RAGService ragService;
    
    @Autowired
    private OpenAiChatModel openAiModel;
    
    private ChatClient evaluatorClient;
    private RelevancyEvaluator relevancyEvaluator;
    
    @BeforeEach
    void setUp() {
        // Create a separate ChatClient for evaluating responses
        evaluatorClient = ChatClient.create(openAiModel);
        
        // Create RelevancyEvaluator for testing RAG response quality
        relevancyEvaluator = new RelevancyEvaluator(ChatClient.builder(openAiModel));
    }
    
    /**
     * Helper method to evaluate if a response is relevant using Spring AI's RelevancyEvaluator
     */
    private void evaluateRelevancy(String question, ChatResponse chatResponse) {
        EvaluationRequest evaluationRequest = new EvaluationRequest(
            question,
            chatResponse.getMetadata().get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS),
            chatResponse.getResult().getOutput().getText()
        );
        
        EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(evaluationRequest);
        assertTrue(evaluationResponse.isPass(), 
            "Response should be relevant to the question. Evaluation details: " + evaluationResponse);
    }

    @Test
    void ragFromWikipediaInfo() {
        // Query about Spring (should return relevant info)
        String question = "What is the latest version of the Spring Framework?";
        ChatResponse chatResponse = ragService.queryWithResponse(question);
        String response = chatResponse.getResult().getOutput().getText();

        System.out.println("RAG Response about Spring:");
        System.out.println(response);

        // Basic assertions
        assertNotNull(response);
        assertFalse(response.isEmpty());
        
        // Use Spring AI's RelevancyEvaluator to validate response quality
        evaluateRelevancy(question, chatResponse);
    }

    @Test
    void ragFromPdfInfo() {
        // Query about the World Economic Forum report
        String question = """
                What are the most transformative technology trends expected to
                reshape global labor markets by 2030, and how does AI rank among them?
                """;
        ChatResponse chatResponse = ragService.queryWithResponse(question);
        String response = chatResponse.getResult().getOutput().getText();

        System.out.println("\nRAG Response about WEF Report:");
        System.out.println(response);

        // Basic assertions
        assertNotNull(response);
        assertFalse(response.isEmpty());
        
        // Use Spring AI's RelevancyEvaluator to validate response quality
        evaluateRelevancy(question, chatResponse);
    }

    @Test
    void outOfScopeQuery() {
        String outOfScopeQuestion = "How do I implement GraphQL in Spring?";
        String outOfScopeResponse = ragService.query(outOfScopeQuestion);

        System.out.println("\nOut of scope RAG Response:");
        System.out.println(outOfScopeResponse);

        assertNotNull(outOfScopeResponse);
        
        // Use AI to evaluate if the response properly indicates lack of knowledge
        String evaluationPrompt = String.format("""
            Does the following response properly indicate that the system doesn't have enough 
            information to answer the question, or that the question is outside its knowledge base?
            
            Response to evaluate: "%s"
            
            Answer with only "true" or "false".
            """, outOfScopeResponse.replace("\"", "\\\""));
            
        String evaluation = evaluatorClient.prompt(evaluationPrompt).call().content();
        
        assertTrue(
            evaluation.trim().toLowerCase().contains("true"),
            "AI evaluation failed - Response should indicate lack of information. " +
            "Evaluation: " + evaluation + ", Original response: " + outOfScopeResponse
        );
    }
}
```

Add the required imports to your test class:

```java
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
```

#### 12.6.3 Benefits of Using RelevancyEvaluator

1. **Semantic Evaluation**: Goes beyond simple null/empty checks to validate actual response quality
2. **Automated Quality Assurance**: Uses AI to evaluate AI responses, providing consistent evaluation criteria
3. **Spring AI Specific**: Demonstrates a unique feature that differentiates Spring AI from other frameworks
4. **Educational Value**: Shows students how to build robust, testable AI applications
5. **Production Ready**: Provides a pattern for validating RAG systems in production environments

The RelevancyEvaluator uses the original question, the retrieved document context, and the generated response to determine if the AI's answer is actually relevant and helpful. This is much more robust than string matching and provides a scalable way to validate RAG system performance.

### Key Benefits of This Implementation

1. **Automated Document Processing**: Uses Spring AI's document readers to handle HTML parsing automatically.
2. **Efficient Chunking**: TokenTextSplitter breaks documents into appropriate chunks for vector embedding.
3. **Proper Separation of Concerns**: Configuration, service, and data loading are properly separated.
4. **Profile-Based Activation**: The RAG system only loads when the profile is active.
5. **Spring AI's Built-in RAG Support**: QuestionAnswerAdvisor handles the complex RAG workflow for you.
6. **Quality Validation**: RelevancyEvaluator provides semantic validation of response quality.

The RAG system you've built can be extended with additional knowledge sources by adding more URLs or document readers to the configuration.

### 12.7 Incorporating PDF Documents into RAG

While web content is easily accessible using the JsoupDocumentReader, many valuable information sources exist as PDF documents. Let's extend our RAG system to incorporate PDF documents:

```kotlin
// If it's not already there, add the required PDF document reader dependency in build.gradle.kts
dependencies {
    // Existing dependencies...
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")
}
```

First, place your PDF files in a resources directory, such as `src/main/resources/pdfs/`.

Then, update your configuration to process these PDF files. The following example shows only one
file, but you can add as many as you need by changing the `Resource` to a collection or array
of resources and looping through them.

```java
@Configuration
public class AppConfig {
    // Existing URLs and configuration...

    // Reference the PDF file from resources
    @Value("classpath:/pdfs/your_document.pdf")
    private Resource pdfDocument;

    @Bean
    @Profile("rag")
    ApplicationRunner loadVectorStore(VectorStore vectorStore) {
        return args -> {
            // Process URLs (already implemented)
            List.of(SPRING_URL /*, other URLs */).forEach(url -> {
                // Existing URL processing code...
            });

            // Add PDF to the vector store
            try {
                System.out.println("Processing PDF document (this may take a few minutes)...");
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfDocument);

                List<Document> pdfDocuments = pdfReader.get();
                System.out.println("Extracted " + pdfDocuments.size() + " documents from PDF");

                List<Document> pdfChunks = splitter.apply(pdfDocuments);
                System.out.println("Split into " + pdfChunks.size() + " chunks");

                vectorStore.add(pdfChunks);
                System.out.println("PDF processing complete!");
            } catch (Exception e) {
                System.err.println("Error processing PDF: " + e.getMessage());
            }
        };
    }
}
```

Now create a test that specifically focuses on information contained in your PDF:

```java
@Test
void ragFromPdfInfo() {
    // Query about content that should be in your PDF
    String question = "What are the main topics covered in the PDF document?";
    String response = ragService.query(question);

    System.out.println("\nRAG Response from PDF content:");
    System.out.println(response);

    assertNotNull(response);
    assertFalse(response.isEmpty());
}
```

**Important notes about PDF processing:**

1. **Performance**: Processing PDFs can be slow, especially for large documents. Consider:
   - Processing only the most relevant pages
   - Implementing a caching mechanism for the vector store
   - Running PDF processing during application startup rather than on-demand

2. **PDF Compatibility**: Some PDFs may have formatting or security settings that make extraction difficult. If you encounter issues:
   - Try alternative PDF readers or libraries
   - Convert problematic PDFs to text format first
   - Use OCR tools for scanned documents

3. **Memory Usage**: Large PDFs can consume significant memory. Monitor your application's memory usage and adjust your JVM settings if necessary.

4. **Production Considerations**: For a production RAG system:
   - Implement persistent storage for your vector store
   - Consider background processing for document ingestion
   - Add monitoring for embedding and processing performance

[↑ Back to table of contents](#table-of-contents)

## Lab 13: Redis Vector Store for RAG

In production environments, you often need a persistent, scalable vector store instead of the in-memory `SimpleVectorStore`. Redis provides an excellent option for a production-ready vector store. This lab will guide you through setting up Redis as your vector store for the RAG system.

**Profile Usage**: This lab demonstrates proper profile separation where Redis configuration is isolated to the `redis` profile. Redis is only activated when explicitly enabled using `--spring.profiles.active=rag,redis`, preventing unnecessary Redis dependencies for basic AI functionality.

### 13.1 Prerequisites

To use Redis as a vector store, you need a running Redis instance. The easiest way to get started is with Docker:

```bash
docker run -p 6379:6379 redis/redis-stack:latest
```

This command starts Redis Stack, which includes Redis and the necessary vector search capabilities.

### 13.2 Update Configuration

Create a dedicated Redis profile in `application-redis.properties`:

```properties
# Redis Profile Configuration
# This profile enables Redis vector store for RAG functionality
# Use with: --spring.profiles.active=rag,redis

# Redis vector store settings
spring.ai.vectorstore.redis.initialize-schema=true
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.username=default
spring.data.redis.password=

# Redis vector store specific configuration
# Enables RedisVectorStore instead of SimpleVectorStore
```

You also need to add the Redis dependencies to your `build.gradle.kts`:

```kotlin
implementation("org.springframework.ai:spring-ai-starter-vector-store-redis")
```

That's sufficient to create and configure a Redis vector store. The Spring AI Redis integration will handle the connection and schema creation for you.

### 13.3 Modify AppConfig to Support Redis

Modify your AppConfig class to support switching between `SimpleVectorStore` and Redis using profiles:

```java
@Configuration
public class AppConfig {
    private static final String FEUD_URL = "https://en.wikipedia.org/wiki/Drake%E2%80%93Kendrick_Lamar_feud";
    private static final String SPRING_URL = "https://en.wikipedia.org/wiki/Spring_Framework";

    private final TextSplitter splitter = new TokenTextSplitter();

    @Value("classpath:/pdfs/WEF_Future_of_Jobs_Report_2025.pdf")
    private Resource jobsReport2025;

    @Bean
    @Profile("rag")
    ApplicationRunner loadVectorStore(VectorStore vectorStore) {
        return args -> {
            System.out.println("Using vector store: " + vectorStore.getClass().getSimpleName());

            // Check if we're using Redis and if data already exists
            boolean isRedisStore = vectorStore.getClass().getSimpleName().toLowerCase().contains("redis");
            boolean dataExists;

            System.out.println("Using vector store class: " + vectorStore.getClass().getName());
            System.out.println("Redis detection enabled: " + isRedisStore);

            if (isRedisStore) {
                // Sample query to check if data exists by looking for existing Spring Framework content
                try {
                    // Simple approach: search for something we know should be there
                    System.out.println("Checking if data exists by searching for 'Spring Framework'...");
                    var results = vectorStore.similaritySearch("Spring Framework");
                    dataExists = !results.isEmpty();
                    System.out.println("Search returned " + results.size() + " results");

                    if (dataExists) {
                        System.out.println("Data already exists in Redis vector store - skipping data loading");
                        return;
                    }
                } catch (Exception e) {
                    // If the search fails, it likely means the data doesn't exist yet
                    System.out.println("No existing data found in Redis vector store");
                }
            }

            System.out.println("Loading data into vector store");

            // Process URLs
            List.of(FEUD_URL, SPRING_URL).forEach(url -> {
                // Fetch HTML content using Jsoup
                List<Document> documents = new JsoupDocumentReader(url).get();
                System.out.println("Fetched " + documents.size() + " documents from " + url);

                // Add source metadata to help identify content later
                documents.forEach(doc -> {
                    String source = url.contains("Drake") ? "drake_feud" : "spring_framework";
                    doc.getMetadata().put("source", source);
                });

                // Split the document into chunks
                List<Document> chunks = splitter.apply(documents);
                System.out.println("Split into " + chunks.size() + " chunks");

                // Add the chunks to the vector store
                vectorStore.add(chunks);
            });

            try {
                // Add PDF to the vector store
                System.out.println("Processing PDF document (this may take a few minutes)...");

                // Process a specific page range for better performance
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(jobsReport2025);

                List<Document> pdfDocuments = pdfReader.get();
                System.out.printf("Fetched %d documents from %s%n", pdfDocuments.size(), jobsReport2025.getFilename());

                // Add source metadata to help identify PDF content
                pdfDocuments.forEach(doc -> {
                    doc.getMetadata().put("source", "wef_jobs_report");
                    doc.getMetadata().put("type", "pdf");
                });

                List<Document> pdfChunks = splitter.apply(pdfDocuments);
                System.out.println("Split into " + pdfChunks.size() + " chunks");

                vectorStore.add(pdfChunks);
                System.out.println("PDF processing complete!");
            } catch (Exception e) {
                System.err.println("Error processing PDF: " + e.getMessage());
                throw new RuntimeException(e);
            }
        };
    }

    @Bean
    @Profile("!redis")
    VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }

    @Bean
    @Profile("redis")
    VectorStore redisVectorStore(EmbeddingModel embeddingModel) {
        // Spring AI 1.1.0 requires JedisPooled as first parameter
        return RedisVectorStore.builder(new JedisPooled("localhost", 6379), embeddingModel)
                .indexName("spring-ai-index")
                .initializeSchema(true)
                .build();
    }
}
```

**Important Note for Spring AI 1.1.0:** The `RedisVectorStore` builder now requires a `JedisPooled` instance as the first parameter. You'll need to add the import:

```java
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import redis.clients.jedis.JedisPooled;
```

The key changes are:
1. Using the `@Profile("!redis")` annotation to only create the SimpleVectorStore when Redis is not active
2. Adding data detection to check if vectors already exist in Redis
3. Adding metadata tagging to identify the source of each document
4. Implementing a skip mechanism to avoid reprocessing PDF documents when data exists
5. Printing detailed information about the vector store being used

The Redis data detection feature is particularly valuable as it:
- Saves significant time by avoiding reprocessing large PDF documents
- Prevents redundant data from being added to the vector store
- Makes the application more efficient when restarting

### 13.4 Update RAGTests to Use Redis

Modify your test class to use both the "rag" and "redis" profiles:

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"rag","redis"})
public class RAGTests {

    @Autowired
    private RAGService ragService;

    @Test
    void ragFromWikipediaInfo() {
        // Query about Spring (should return relevant info)
        String question = "What is the latest version of the Spring Framework?";
        String response = ragService.query(question);

        System.out.println("RAG Response about Spring:");
        System.out.println(response);

        // Assertions for Chat Client API query
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }

    // Additional tests...
}
```

### 13.5 Running the Tests

Run the tests with both profiles activated:

```bash
./gradlew test --tests RAGTests -Dspring.profiles.active=rag,redis
```

[↑ Back to table of contents](#table-of-contents)

## Lab 14: Model Context Protocol (MCP) - Client

The Model Context Protocol (MCP) is a standardized protocol for communication between AI applications and external tools. Spring AI provides comprehensive support for both MCP clients and servers. In this lab, you'll learn how to create an MCP client that connects to external MCP servers.

### 14.1 Understanding MCP

MCP enables:
- Standardized tool exposure and discovery
- Multiple transport mechanisms (STDIO, SSE, HTTP)
- Dynamic tool registration and updates
- Secure, structured communication between AI systems and tools

### 14.2 Adding MCP Client Dependencies

First, add the MCP client starter to your `build.gradle.kts`:

```kotlin
dependencies {
    // Existing dependencies...
    
    // MCP Client support
    implementation("org.springframework.ai:spring-ai-starter-mcp-client")
}
```

### 14.3 Creating a Simple MCP Client Configuration

Create a configuration file `src/main/resources/application-mcp.properties`:

```properties
# MCP Client Configuration
spring.ai.mcp.client.enabled=true
spring.ai.mcp.client.name=training-mcp-client
spring.ai.mcp.client.version=1.0.0

# Configure STDIO connection to Context7 MCP server for library documentation
# Context7 provides up-to-date documentation for libraries and frameworks
spring.ai.mcp.client.stdio.connections.context7.command=npx
spring.ai.mcp.client.stdio.connections.context7.args=-y,@upstash/context7-mcp@latest
```

### 14.4 Using MCP Tools in Your Application

Create a test class to demonstrate MCP client usage with Context7:

```java
@SpringBootTest
@ActiveProfiles("mcp")
@EnabledIfEnvironmentVariable(named = "OPENAI_API_KEY", matches = ".+")
public class McpClientTests {

    @Autowired
    private ChatModel chatModel;  // Uses primary ChatModel (OpenAI)

    @Autowired(required = false)
    private List<ToolCallback> mcpTools;  // Auto-discovered MCP tools

    private ChatClient chatClient;

    @BeforeEach
    void setUp() {
        // Create a chat client with MCP tools if available
        if (mcpTools != null && !mcpTools.isEmpty()) {
            chatClient = ChatClient.builder(chatModel)
                    .defaultToolCallbacks(mcpTools)
                    .build();
        } else {
            chatClient = ChatClient.builder(chatModel).build();
        }
    }

    @Test
    void listAvailableTools() {
        if (mcpTools != null) {
            System.out.println("Available MCP tools: " + mcpTools.size());
            mcpTools.forEach(tool -> {
                System.out.println("- Tool callback available: " + tool.getClass().getSimpleName());
            });

            assertFalse(mcpTools.isEmpty(), "Should have discovered MCP tools when servers are configured");
        } else {
            System.out.println("No MCP tools discovered. This is expected if no MCP servers are configured.");
        }
    }

    @Test
    void lookupLibraryDocumentation() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping Context7 test - no MCP tools available");
            return;
        }

        try {
            // Use Context7 to look up Spring AI documentation
            String response = chatClient.prompt()
                    .user("Using Context7, find documentation about ChatClient in Spring AI. " +
                          "What are the main methods available?")
                    .call()
                    .content();

            System.out.println("Context7 documentation response: " + response);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        } catch (Exception e) {
            System.out.println("Context7 test failed: " + e.getMessage());
        }
    }

    @Test
    void getFrameworkExamples() {
        if (mcpTools == null || mcpTools.isEmpty()) {
            System.out.println("Skipping Context7 example test - no MCP tools available");
            return;
        }

        try {
            // Ask for code examples from documentation
            String response = chatClient.prompt()
                    .user("Using Context7, show me examples of how to use prompt templates " +
                          "in Spring AI. Include any code snippets from the documentation.")
                    .call()
                    .content();

            System.out.println("Context7 examples response: " + response);
            assertNotNull(response);
            assertFalse(response.isEmpty());
        } catch (Exception e) {
            System.out.println("Context7 examples test failed: " + e.getMessage());
        }
    }
}
```

### 14.5 Connecting to Multiple MCP Servers

You can connect to multiple MCP servers simultaneously. This example configures both Context7 for library documentation and Tavily for AI-optimized web search:

```properties
# Context7 server for library documentation lookup
spring.ai.mcp.client.stdio.connections.context7.command=npx
spring.ai.mcp.client.stdio.connections.context7.args=-y,@upstash/context7-mcp@latest

# Tavily search server (requires TAVILY_API_KEY environment variable)
# Tavily provides AI-optimized search results, ideal for RAG and AI agents
spring.ai.mcp.client.stdio.connections.tavily.command=npx
spring.ai.mcp.client.stdio.connections.tavily.args=-y,tavily-mcp@latest
```

To use Tavily, you'll need to set the `TAVILY_API_KEY` environment variable. You can get a free API key at https://tavily.com.

Here's a test that demonstrates using both servers together:

```java
@Test
void combineDocumentationAndWebSearch() {
    if (mcpTools == null || mcpTools.isEmpty()) {
        System.out.println("Skipping combined test - no MCP tools available");
        return;
    }

    try {
        // First, get documentation context
        String docsResponse = chatClient.prompt()
                .user("Using Context7, what is the recommended way to implement " +
                      "RAG (Retrieval-Augmented Generation) in Spring AI?")
                .call()
                .content();

        System.out.println("Documentation response: " + docsResponse);

        // Then, search for recent community discussions or updates
        String searchResponse = chatClient.prompt()
                .user("Using Tavily, search for recent blog posts or tutorials about " +
                      "Spring AI RAG implementation best practices from 2024-2025.")
                .call()
                .content();

        System.out.println("Web search response: " + searchResponse);

        assertNotNull(docsResponse);
        assertNotNull(searchResponse);
    } catch (Exception e) {
        System.out.println("Combined test failed: " + e.getMessage());
    }
}
```

### 14.6 Using SSE Transport

For servers that support Server-Sent Events (SSE), you can use HTTP-based connections:

```properties
# SSE connection to a local MCP server
spring.ai.mcp.client.sse.connections.weather.url=http://localhost:8080
```

### 14.7 Advanced: Custom MCP Client Configuration

Create a configuration class for more control:

```java
@Configuration
@Profile("mcp")
public class McpClientConfig {
    
    @Bean
    McpSyncClientCustomizer mcpClientCustomizer() {
        return (serverName, spec) -> {
            // Configure timeout
            spec.requestTimeout(Duration.ofSeconds(30));
            
            // Add tool change listener
            spec.toolsChangeConsumer(tools -> {
                System.out.println("Tools updated for " + serverName + ": " + tools.size() + " tools available");
            });
            
            // Add logging
            spec.loggingConsumer(log -> {
                System.out.println("[MCP Log] " + log.level() + ": " + log.message());
            });
        };
    }
}
```

### 14.8 Exercise: Research Assistant

Create a test that combines Context7 and Tavily to build a research assistant that can answer questions using both official documentation and current web content:

```java
@Test
void researchAssistant() {
    // TODO: Create a prompt that asks about a Spring AI feature
    // TODO: First use Context7 to get official documentation
    // TODO: Then use Tavily to find real-world examples and discussions
    // TODO: Combine the results into a comprehensive answer

    // Example question to research:
    // "How do I implement function calling (tools) in Spring AI,
    //  and what are some real-world use cases?"
}
```

**Bonus Challenge**: Create a service class that abstracts the research pattern, automatically querying both documentation and web sources for any given topic.

[↑ Back to table of contents](#table-of-contents)

## Lab 15: Model Context Protocol (MCP) - Server

In this lab, you'll create your own MCP server that exposes custom tools to AI clients like Claude Desktop or your Spring AI applications.

### 15.1 Adding MCP Server Dependencies

Add the MCP server starter to your `build.gradle.kts`:

```kotlin
dependencies {
    // Existing dependencies...
    
    // MCP Server support (choose one based on your needs)
    implementation("org.springframework.ai:spring-ai-starter-mcp-server")  // For STDIO
    // OR
    implementation("org.springframework.ai:spring-ai-starter-mcp-server-webmvc")  // For SSE with Spring MVC
}
```

### 15.2 Creating Your First MCP Server

Create a simple calculator MCP server:

```java
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

@Service
public class CalculatorService {
    
    @Tool(description = "Add two numbers together")
    public double add(double a, double b) {
        return a + b;
    }
    
    @Tool(description = "Multiply two numbers")
    public double multiply(double a, double b) {
        return a * b;
    }
    
    @Tool(description = "Calculate the square root of a number")
    public double sqrt(double number) {
        if (number < 0) {
            throw new IllegalArgumentException("Cannot calculate square root of negative number");
        }
        return Math.sqrt(number);
    }
    
    @Tool(description = "Calculate compound interest given principal, annual rate (as percentage), years, and compounding frequency per year")
    public CompoundInterestResult calculateCompoundInterest(
            double principal,
            double annualRate,
            int years,
            int compoundingFrequency) {
        
        double rate = annualRate / 100;
        double amount = principal * Math.pow(1 + rate / compoundingFrequency, 
                                           compoundingFrequency * years);
        double interest = amount - principal;
        
        return new CompoundInterestResult(principal, amount, interest, years, annualRate);
    }
    
    record CompoundInterestResult(
        double principal,
        double finalAmount,
        double totalInterest,
        int years,
        double annualRate
    ) {}
}
```

### 15.3 Configuring the MCP Server

Create an MCP server configuration class:

```java
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Configuration class for MCP Server functionality (Lab 15).
 * 
 * This configuration is activated when the 'mcp-server' profile is enabled.
 * The CalculatorService @Tool annotated methods are automatically discovered
 * by Spring AI's MCP server auto-configuration.
 */
@Configuration
@Profile("mcp-server")
public class McpServerConfig {
    
    /**
     * Application runner that logs MCP server startup information.
     * This helps developers understand what tools are being exposed.
     */
    @Bean
    public ApplicationRunner mcpServerStartupLogger() {
        return args -> {
            System.out.println("\n=== MCP Server Started ===");
            System.out.println("Profile: mcp-server");
            System.out.println("Available tools from CalculatorService:");
            System.out.println("  • add(double, double) - Add two numbers");
            System.out.println("  • subtract(double, double) - Subtract numbers");
            System.out.println("  • multiply(double, double) - Multiply numbers");
            System.out.println("  • divide(double, double) - Divide numbers");
            System.out.println("  • sqrt(double) - Square root");
            System.out.println("  • power(double, double) - Power calculation");
            System.out.println("  • calculateCompoundInterest(...) - Compound interest");
            System.out.println("  • calculatePercentage(double, double) - Percentage");
            System.out.println("\nConnect to this server using:");
            System.out.println("  • Claude Desktop MCP configuration");
            System.out.println("  • STDIO transport mode");
            System.out.println("  • SSE transport mode (uncomment config in properties)");
            System.out.println("========================\n");
        };
    }
    
    // The CalculatorService with @Tool annotated methods is automatically
    // discovered by Spring AI's MCP server auto-configuration.
    // No explicit tool registration is required.
}
```

Configure the server in `application-mcp-server.properties`:

```properties
# MCP Server Configuration
spring.ai.mcp.server.name=calculator-server
spring.ai.mcp.server.version=1.0.0
spring.ai.mcp.server.type=SYNC

# For STDIO mode (works with Claude Desktop)
spring.ai.mcp.server.stdio=true
spring.main.web-application-type=none
logging.pattern.console=

# For SSE mode (HTTP-based)
# spring.ai.mcp.server.stdio=false
# spring.ai.mcp.server.sse-message-endpoint=/mcp/messages
```

### 15.4 Testing Your MCP Server

Create a test to verify your server works:

```java
@SpringBootTest
@ActiveProfiles("mcp-server")
public class McpServerTests {
    
    @Autowired
    private CalculatorService calculatorService;
    
    @Test
    void contextLoads() {
        // Basic test to ensure Spring context loads with MCP server profile
        assertNotNull(calculatorService);
    }
    
    @Test
    void calculatorServiceFunctionality() {
        // Test the calculator service directly
        assertEquals(5.0, calculatorService.add(2, 3), 0.001);
        assertEquals(1.0, calculatorService.subtract(3, 2), 0.001);
        assertEquals(6.0, calculatorService.multiply(2, 3), 0.001);
        assertEquals(2.0, calculatorService.divide(6, 3), 0.001);
        assertEquals(3.0, calculatorService.sqrt(9), 0.001);
        assertEquals(8.0, calculatorService.power(2, 3), 0.001);
        assertEquals(15.0, calculatorService.calculatePercentage(15, 100), 0.001);
    }
    
    @Test
    void calculatorServiceErrorHandling() {
        // Test error conditions
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.divide(5, 0));
        
        assertThrows(IllegalArgumentException.class, () -> 
            calculatorService.sqrt(-4));
    }
    
    @Test
    void verifyMcpServerProfile() {
        // Test that the MCP server profile is active and the CalculatorService is available
        assertNotNull(calculatorService, "CalculatorService should be available");
        
        System.out.println("MCP Server profile is active");
        System.out.println("CalculatorService is available with @Tool annotated methods:");
        System.out.println("- add(double, double)");
        System.out.println("- subtract(double, double)"); 
        System.out.println("- multiply(double, double)");
        System.out.println("- divide(double, double)");
        System.out.println("- sqrt(double)");
        System.out.println("- power(double, double)");
        System.out.println("- calculateCompoundInterest(double, double, int, int)");
        System.out.println("- calculatePercentage(double, double)");
        
        System.out.println("\nNote: Spring AI MCP auto-configuration should automatically");
        System.out.println("discover and expose these @Tool methods to MCP clients.");
    }
}
```

### 15.5 Integrating with Claude Desktop

To use your server with Claude Desktop:

1. Build your server JAR:
```bash
./gradlew bootJar
```

2. Add to Claude Desktop configuration (`~/Library/Application Support/Claude/claude_desktop_config.json` on macOS):

```json
{
  "mcpServers": {
    "calculator": {
      "command": "java",
      "args": [
        "-Dspring.profiles.active=mcp-server",
        "-jar",
        "/path/to/your/build/libs/springaicourse-0.0.1-SNAPSHOT.jar"
      ]
    }
  }
}
```

3. Restart Claude Desktop and test your tools!

### 15.6 Advanced: Adding Resources and Prompts

MCP servers can also provide resources (data) and prompt templates:

```java
@Configuration
public class McpServerAdvancedConfig {
    
    @Bean
    public List<McpServerFeatures.SyncResourceSpecification> resources() {
        // Define a resource that provides system information
        var systemInfoResource = new McpSchema.Resource(
            "system-info",
            "Current system information",
            "application/json"
        );
        
        return List.of(
            new McpServerFeatures.SyncResourceSpecification(
                systemInfoResource,
                (exchange, request) -> {
                    var info = Map.of(
                        "java_version", System.getProperty("java.version"),
                        "os", System.getProperty("os.name"),
                        "timestamp", Instant.now().toString()
                    );
                    
                    return new McpSchema.ReadResourceResult(
                        List.of(new McpSchema.ResourceContent(
                            request.uri(),
                            "application/json",
                            new ObjectMapper().writeValueAsString(info)
                        ))
                    );
                }
            )
        );
    }
    
    @Bean
    public List<McpServerFeatures.PromptSpecification> prompts() {
        // Define reusable prompt templates
        return List.of(
            new McpServerFeatures.PromptSpecification(
                new McpSchema.Prompt(
                    "investment-advisor",
                    "Calculate investment returns",
                    List.of(
                        new McpSchema.PromptArgument(
                            "principal",
                            "Initial investment amount",
                            true
                        ),
                        new McpSchema.PromptArgument(
                            "years",
                            "Investment period in years",
                            true
                        )
                    )
                ),
                args -> new McpSchema.GetPromptResult(
                    "Calculate compound interest for $" + args.get("principal") + 
                    " over " + args.get("years") + " years at various rates",
                    List.of(
                        new McpSchema.PromptMessage(
                            McpSchema.Role.user,
                            new McpSchema.TextContent(
                                "Calculate returns for conservative (4%), moderate (7%), " +
                                "and aggressive (10%) investment strategies"
                            )
                        )
                    )
                )
            )
        );
    }
}
```

### 15.7 Exercise: Create a System Diagnostics MCP Server

Building on the calculator example, create an MCP server that provides system diagnostic tools:

```java
@Service
public class SystemDiagnosticsService {
    
    @Tool(description = "Get current memory usage statistics")
    public MemoryStats getMemoryUsage() {
        // TODO: Implement memory statistics
        // Hint: Use Runtime.getRuntime() or ManagementFactory.getMemoryMXBean()
    }
    
    @Tool(description = "List running Java threads")
    public List<ThreadInfo> getActiveThreads() {
        // TODO: Implement thread listing
        // Hint: Use ManagementFactory.getThreadMXBean()
    }
    
    @Tool(description = "Get system properties filtered by prefix (e.g., 'java' for Java-related properties)")
    public Map<String, String> getSystemProperties(String prefix) {
        // TODO: Implement filtered system properties
    }
}
```

### 15.8 Best Practices for MCP Servers

1. **Tool Naming**: Use clear, action-oriented names (e.g., `calculateTax` not `tax`)
2. **Descriptions**: Provide detailed descriptions for tools and parameters
3. **Error Handling**: Return meaningful error messages that AI can understand
4. **Validation**: Validate inputs and provide clear constraints
5. **Logging**: Use Spring's logging for debugging (but configure appropriately for STDIO mode)
6. **Testing**: Test both the tools themselves and their MCP integration

[↑ Back to table of contents](#table-of-contents)

## Conclusion

Congratulations! You've completed a comprehensive tour of Spring AI's capabilities. You've learned how to:

- Interact with LLMs through Spring AI's abstraction layer
- Stream responses for a better user experience
- Extract structured data from LLM responses
- Use prompt templates for consistent prompting
- Maintain conversation state with chat memory
- Work with vision capabilities for image analysis
- Generate images using AI models
- Process audio with text-to-speech and speech-to-text
- Extend AI capabilities with custom tools
- Enhance AI responses with external content using prompt stuffing
- Build a Retrieval-Augmented Generation (RAG) system for accurate, grounded responses
- Use Redis as a persistent vector store for production RAG applications
- Create MCP clients to connect to external tool servers
- Build MCP servers to expose your own tools to AI systems

These skills provide a solid foundation for building AI-powered applications using the Spring ecosystem.