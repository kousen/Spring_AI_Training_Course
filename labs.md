# Spring AI Course Labs

This series of labs will guide you through building a Spring AI application that uses various capabilities of large language models via the Spring AI abstraction layer. By the end of these exercises, you'll have hands-on experience with text generation, structured data extraction, prompt templates, chat memory, vision capabilities, and more.

> **Note:** This project uses Spring Boot 3.4.5 and Spring AI 1.0.0-RC1. Spring AI 1.0.0-RC1 includes significant API changes, including using builder patterns for constructing advisors like `MessageChatMemoryAdvisor`.

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
- [Lab 9: AI Tools](#lab-9-ai-tools)
- [Lab 10: Audio Capabilities](#lab-10-audio-capabilities)
- [Lab 11: Refactoring for Production](#lab-11-refactoring-for-production)
- [Lab 12: Retrieval-Augmented Generation (RAG)](#lab-12-retrieval-augmented-generation-rag)
- [Lab 13: Redis Vector Store for RAG (Optional)](#lab-13-redis-vector-store-for-rag-optional)
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

3. Check that the project builds successfully:
   ```bash
   ./gradlew build
   ```

## Lab 1: Basic Chat Interactions <a id="lab-1-basic-chat-interactions"></a>

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

## Lab 2: Request and Response Logging <a id="lab-2-request-and-response-logging"></a>

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

## Lab 3: Streaming Responses <a id="lab-3-streaming-responses"></a>

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

## Lab 4: Structured Data Extraction <a id="lab-4-structured-data-extraction"></a>

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

## Lab 5: Prompt Templates <a id="lab-5-prompt-templates"></a>

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

Then create a test that loads this template:

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

## Lab 6: Chat Memory <a id="lab-6-chat-memory"></a>

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
            .user(u -> u
                    .text("My name is Inigo Montoya. You killed my father. Prepare to die."))
            .call()
            .content();
    System.out.println(answer1);

    System.out.println("Second query:");
    String answer2 = chatClient.prompt()
            .user(u -> u.text("Who am I?"))
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

Create a test that demonstrates how to make conversations stateful using ChatMemory:

```java
@Test
void requestsWithMemory() {
    ChatClient chatClient = ChatClient.create(model);

    System.out.println("Initial query with memory:");
    String answer1 = chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(memory).build())
            .user(u -> u
                    .text("My name is Inigo Montoya. You killed my father. Prepare to die."))
            .call()
            .content();
    System.out.println(answer1);

    System.out.println("Second query with memory:");
    String answer2 = chatClient.prompt()
            .advisors(MessageChatMemoryAdvisor.builder(memory).build())
            .user(u -> u.text("Who am I?"))
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

## Lab 7: Vision Capabilities <a id="lab-7-vision-capabilities"></a>

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

## Lab 8: Image Generation <a id="lab-8-image-generation"></a>

Create a test that generates an image. Note that you can
autowire in the `OpenAiImageModel` bean:

```java
@Test
void imageGenerator(@Autowired OpenAiImageModel imageModel) {
    String prompt = """
            A warrior cat rides a dragon into battle""";
    var imagePrompt = new ImagePrompt(prompt);
    ImageResponse imageResponse = imageModel.call(imagePrompt);
    
    System.out.println(imageResponse);
}
```

The response object will contain an `Image` that includes a URL
to the generated image. You can use this URL to display the image
in a web application or save it to a file. The image is only 
available for a limited time, so be sure to download it
if you want to keep it.

Alternatively, you can configure the request to ask for a 
Base 64 encoded image instead of a URL. The `ImageResponse` 
object will then contain a Base 64 encoded string
that represents the image. You can use this string to display
the image in a web application or save it to a file. To save
the image to a file, you can decode the Base 64 string and write
it to a file. 

```java
    @Test
void imageGeneratorBase64(@Autowired OpenAiImageModel imageModel) throws IOException {
   String prompt = """
           A warrior cat rides a dragon into battle""";

   var imageOptions = OpenAiImageOptions.builder()
           .responseFormat("b64_json")
           .build();
   var imagePrompt = new ImagePrompt(prompt, imageOptions);
   ImageResponse imageResponse = imageModel.call(imagePrompt);
   Image image = imageResponse.getResult().getOutput();
   assertNotNull(image);

   // Decode the base64 to bytes
   byte[] imageBytes = Base64.getDecoder().decode(image.getB64Json());

   // Write to file (e.g., PNG)
   Files.write(Path.of("src/main/resources","output_image.png"), imageBytes);
   System.out.println("Image saved as output_image.png in src/main/resources");
}
```

You can change the file name and format as needed.

## Lab 9: AI Tools <a id="lab-9-ai-tools"></a>

### 9.1 Create a Tool

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

### 9.2 Use the Tools

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

## Lab 10: Audio Capabilities <a id="lab-10-audio-capabilities"></a>

### 10.1 Text-to-Speech (TTS)

Create a test that generates speech from text:

```java
@Test
void textToSpeech(@Autowired OpenAiAudioSpeechModel speechModel) {
    String text = "Welcome to Spring AI, a powerful framework for integrating AI into your Spring applications.";
    
    OpenAiAudioSpeechOptions options = OpenAiAudioSpeechOptions.builder()
            .voice(OpenAiAudioApi.SpeechRequest.Voice.ALLOY)
            .responseFormat(OpenAiAudioApi.SpeechRequest.AudioResponseFormat.MP3)
            .speed(1.0f)
            .build();
    
    SpeechPrompt prompt = new SpeechPrompt(text, options);
    SpeechResponse response = speechModel.call(prompt);
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

### 10.2 Speech-to-Text (Transcription)

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

## Lab 11: Refactoring for Production <a id="lab-11-refactoring-for-production"></a>

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

## Lab 12: Retrieval-Augmented Generation (RAG) <a id="lab-12-retrieval-augmented-generation-rag"></a>

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
        // Define the instruction prompt
        String instructionPrompt = """
                Answer the question based ONLY on the provided context.
                If the context doesn't contain relevant information, say
                "I don't have enough information to answer this question."
                """;

        // Create a QuestionAnswerAdvisor with the vectorStore
        QuestionAnswerAdvisor advisor = new QuestionAnswerAdvisor(vectorStore);

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

### 12.6 Using a Persistent Vector Store

For long-term persistence and better performance with large documents, you can consider using a persistent vector store like Chroma or PostgreSQL.

Spring AI provides integrations with several vector store options, including:

1. **SimpleVectorStore** (default, in-memory): Easiest to set up but doesn't persist data between application restarts
2. **PostgresVectorStore**: Uses PostgreSQL with the pgvector extension
3. **ChromaVectorStore**: Uses the Chroma vector database
4. **PineconeVectorStore**: Uses Pinecone, a managed vector database service
5. **WeaviateVectorStore**: Uses Weaviate, a vector search engine

Each of these options has different setup requirements and performance characteristics.

For this lab, we're using the SimpleVectorStore for ease of setup, but in production environments, a persistent vector store would typically be preferred.

### Key Benefits of This Implementation

1. **Automated Document Processing**: Uses Spring AI's document readers to handle HTML parsing automatically.
2. **Efficient Chunking**: TokenTextSplitter breaks documents into appropriate chunks for vector embedding.
3. **Proper Separation of Concerns**: Configuration, service, and data loading are properly separated.
4. **Profile-Based Activation**: The RAG system only loads when the profile is active.
5. **Spring AI's Built-in RAG Support**: QuestionAnswerAdvisor handles the complex RAG workflow for you.

The RAG system you've built can be extended with additional knowledge sources by adding more URLs or document readers to the configuration.

### 12.7 Incorporating PDF Documents into RAG

While web content is easily accessible using the JsoupDocumentReader, many valuable information sources exist as PDF documents. Let's extend our RAG system to incorporate PDF documents:

```kotlin
// Add the required PDF document reader dependency in build.gradle.kts
dependencies {
    // Existing dependencies...
    implementation("org.springframework.ai:spring-ai-pdf-document-reader")
}
```

First, place your PDF files in a resources directory, such as `src/main/resources/pdfs/`.

Then, update your configuration to process these PDF files:

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

                // Option 1: Process the entire PDF
                // PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfDocument);

                // Option 2: Process specific pages only (more efficient)
                PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(pdfDocument, 5, 25);

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

## Lab 13: Redis Vector Store for RAG (Optional) <a id="lab-13-redis-vector-store-for-rag-optional"></a>

In production environments, you often need a persistent, scalable vector store instead of the in-memory SimpleVectorStore. Redis provides an excellent option for a production-ready vector store. This lab will guide you through setting up Redis as your vector store for the RAG system.

### 13.1 Prerequisites

To use Redis as a vector store, you need a running Redis instance. The easiest way to get started is with Docker:

```bash
docker run -p 6379:6379 redis/redis-stack:latest
```

This command starts Redis Stack, which includes Redis and the necessary vector search capabilities.

### 13.2 Update Configuration

Update your application.properties with Redis configuration:

```properties
# Redis settings
spring.ai.vectorstore.redis.initialize-schema=true
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.username=default
spring.data.redis.password=
```

### 13.3 Modify AppConfig to Support Redis

Modify your AppConfig class to support switching between SimpleVectorStore and Redis using profiles:

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

            System.out.println("\n###################################################");
            System.out.println("Using vector store class: " + vectorStore.getClass().getName());
            System.out.println("Redis detection enabled: " + isRedisStore);
            System.out.println("###################################################\n");

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
}
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

### 13.6 Performance Considerations

When using Redis for RAG in production, consider these optimizations:

1. **Pre-embedding**: Process and embed documents during off-hours or as a batch job
2. **Batch processing**: Group documents into batches for more efficient embedding
3. **Connection pooling**: Configure appropriate Redis connection pool settings
4. **Monitoring**: Add metrics to track embedding and query performance
5. **Scaling**: Consider Redis Enterprise for performance-critical applications
6. **Persistence**: Configure Redis persistence options to prevent data loss

### 13.7 Redis Vector Store Benefits

Using Redis as your vector store provides several advantages:

1. **Persistence**: Vector embeddings survive application restarts
2. **Speed**: Redis provides fast vector similarity search
3. **Scalability**: Redis can be clustered for larger datasets
4. **Advanced search**: Supports hybrid search combining vector similarity and metadata filtering
5. **Monitoring**: Built-in tools for monitoring performance

## Conclusion <a id="conclusion"></a>

Congratulations! You've completed a comprehensive tour of Spring AI's capabilities. You've learned how to:

- Interact with LLMs through Spring AI's abstraction layer
- Stream responses for a better user experience
- Extract structured data from LLM responses
- Use prompt templates for consistent prompting
- Maintain conversation state with chat memory
- Work with vision capabilities for image analysis
- Generate images using AI models
- Extend AI capabilities with custom tools
- Process audio with text-to-speech and speech-to-text
- Enhance AI responses with external content using prompt stuffing
- Build a Retrieval-Augmented Generation (RAG) system for accurate, grounded responses
- Use Redis as a persistent vector store for production RAG applications

These skills provide a solid foundation for building AI-powered applications using the Spring ecosystem.