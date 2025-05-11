# Spring AI Course Labs

This series of labs will guide you through building a Spring AI application that utilizes various capabilities of large language models via the Spring AI abstraction layer. By the end of these exercises, you'll have hands-on experience with text generation, structured data extraction, prompt templates, chat memory, vision capabilities, and more.

## Setup

1. Make sure you have the following prerequisites:
   - Java 21+
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

## Lab 2: Streaming Responses

### 2.1 Streaming with CountDownLatch

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

### 2.2 Streaming with Reactor Operators

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

## Lab 3: Structured Data Extraction

### 3.1 Create the Data Class

Create a record to represent structured data:

```java
public record ActorFilms(String actor, List<String> movies) {}
```

### 3.2 Single Entity Extraction

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

### 3.3 Collection of Entities

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

## Lab 4: Prompt Templates

### 4.1 Inline Template

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

### 4.2 Template from Resource

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

## Lab 5: Chat Memory

### 5.1 Demonstrating Stateless Behavior

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

### 5.2 Adding Memory to Retain Conversation State

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
            .advisors(new MessageChatMemoryAdvisor(memory))
            .user(u -> u
                    .text("My name is Inigo Montoya. You killed my father. Prepare to die."))
            .call()
            .content();
    System.out.println(answer1);

    System.out.println("Second query with memory:");
    String answer2 = chatClient.prompt()
            .advisors(new MessageChatMemoryAdvisor(memory))
            .user(u -> u.text("Who am I?"))
            .call()
            .content();
    System.out.println(answer2);

    // Verify the model correctly identifies the user as Inigo Montoya
    assertTrue(answer2.toLowerCase().contains("inigo montoya"),
            "The model should remember the user's identity when using memory");
}
```

This example showed how to add chat memory to each individual
request. However, you can also use the `ChatClient` builder
to set the memory advisor for all requests. This is useful
if you want to maintain the conversation state across multiple
requests without having to specify the memory advisor each time.

```java
ChatClient chatClient = ChatClient.builder(model)
        .defaultAdvisors(new MessageChatMemoryAdvisor(memory))
        .build();
```

If you add that to the `setUp` method, you can remove the
`advisors` method from the individual requests.

```java
@BeforeEach
void setUp() {
    // Use builder to add default advisors
    chatClient = ChatClient.builder(model)
            .defaultAdvisors(new MessageChatMemoryAdvisor(memory))
            .build();
}
```

To use that approach, be sure to add a `ChatClient` field
to the test class:

```java
private ChatClient chatClient;
```

## Lab 6: Vision Capabilities

### 6.1 Local Image

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

### 6.2 Remote Image

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

## Lab 7: Image Generation

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

## Lab 8: AI Tools

### 8.1 Create a Tool

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

### 8.2 Use the Tools

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

## Lab 9: Audio Capabilities

### 9.1 Text-to-Speech

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

### 9.2 Speech-to-Text

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

## Lab 10: Refactoring for Production

### 10.1 Create a Common Setup

Refactor your tests to use a common setup method:

```java
@BeforeEach
void setUp() {
    // Use builder to add default advisors if desired
    chatClient = ChatClient.builder(model)
            .defaultAdvisors(
                    new MessageChatMemoryAdvisor(memory),
                    new SimpleLoggerAdvisor())
            .build();

    // Use create for defaults
    chatClient = ChatClient.create(model);
}
```

### 10.2 Add Service Classes

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

### 10.3 Create API Endpoints

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

## Lab 11: Retrieval-Augmented Generation (RAG)

In this lab, you'll build a Retrieval-Augmented Generation (RAG) system using Spring AI's document readers and vector store capabilities. RAG enhances AI responses by retrieving relevant information from a knowledge base before generating answers.

### 11.1 Adding Required Dependencies

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

### 11.2 Setting up the Configuration

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

### 11.3 Creating the RAG Service

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

### 11.4 Testing the RAG System

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

### 11.5 Running with the RAG Profile

To run your application with RAG enabled, set the active profile:

```bash
# Command line
./gradlew bootRun --args='--spring.profiles.active=rag'

# In IntelliJ IDEA
# Edit Run Configuration -> Program arguments: --spring.profiles.active=rag
```

By using the profile approach, you ensure that the RAG system only loads its knowledge base when explicitly enabled, preventing unnecessary processing during regular application use or other tests.

### Key Benefits of This Implementation

1. **Automated Document Processing**: Uses Spring AI's document readers to handle HTML parsing automatically.
2. **Efficient Chunking**: TokenTextSplitter breaks documents into appropriate chunks for vector embedding.
3. **Proper Separation of Concerns**: Configuration, service, and data loading are properly separated.
4. **Profile-Based Activation**: The RAG system only loads when the profile is active.
5. **Spring AI's Built-in RAG Support**: QuestionAnswerAdvisor handles the complex RAG workflow for you.

The RAG system you've built can be extended with additional knowledge sources by adding more URLs or document readers to the configuration.

## Conclusion

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

These skills provide a solid foundation for building AI-powered applications using the Spring ecosystem.