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

Create a test method that sends a simple query to the OpenAI model using Spring AI's ChatClient:

```java
@Test
void simpleQuery() {
    // Get the model from Spring context
    OpenAiChatModel model = /* get the model bean */;
    
    // Create a chat client
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

Create a test that retrieves and displays the full response object including metadata:

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

## Lab 2: Streaming Responses

### 2.1 Streaming with CountDownLatch

Create a test that streams the response using a CountDownLatch:

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

### 2.2 Streaming with Reactor Operators

Create a test that uses Reactor's operators to process the stream:

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

Create a test that extracts a collection of entities:

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

### 4.2 Template from Resource

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

## Lab 5: Chat Memory

### 5.1 Demonstrating Stateless Behavior

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
            "The model should not remember previous conversations without memory")
}
```

### 5.2 Adding Memory to Retain Conversation State

Create a test that demonstrates how to make conversations stateful using ChatMemory:

```java
@Test
void requestsWithMemory() {
    // Get the memory bean from Spring context
    ChatMemory memory = /* get the memory bean */;
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
            "The model should remember the user's identity when using memory")
}
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

## Lab 7: Image Generation

Create a test that generates an image:

```java
@Test
void imageGenerator() {
    OpenAiImageModel imageModel = /* get the image model bean */;
    
    String prompt = """
            A warrior cat rides a dragon into battle""";
    var imagePrompt = new ImagePrompt(prompt);
    ImageResponse imageResponse = imageModel.call(imagePrompt);
    
    System.out.println(imageResponse);
}
```

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

### 8.2 Use the Tool

Create a test that uses the tool:

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

## Lab 9: Audio Capabilities (Optional)

### 9.1 Text-to-Speech

Create a test that generates speech from text:

```java
@Test
void textToSpeech() {
    OpenAiAudioSpeechModel speechModel = /* get the speech model bean */;
    
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

First, add a sample audio file in `src/main/resources/audio/tftjs.mp3`.

Then create a test that transcribes speech to text:

```java
@Test
void speechToText() {
    OpenAiAudioTranscriptionModel transcriptionModel = /* get the transcription model bean */;
    
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

## Lab 10: Refactoring for Production (Bonus)

### 10.1 Create a Common Setup

Refactor your tests to use a common setup method:

```java
@BeforeEach
void setUp() {
    // Use builder to add default advisors
    chatClient = ChatClient.builder(model)
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

## Lab 11: Document-Enhanced Prompting

### 11.1 Prompt Stuffing with External Content

In this lab, you'll learn how to enhance prompts with real-world, up-to-date information by fetching content from the web.

First, make sure you have the appropriate dependencies in your build.gradle.kts:

```kotlin
dependencies {
    // Existing dependencies

    // For RestClient (already included in spring-boot-starter-web)
    // Add the Tavily API client - no official client, we'll use RestClient directly

    // Ensure you have a TAVILY_API_KEY environment variable set
    // You can sign up for a free API key at https://tavily.com/
}
```

Create a utility class to fetch content from a URL using Tavily's content extraction API:

```java
@Component
public class ContentFetcher {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;

    public ContentFetcher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.restClient = RestClient.create();
        this.apiKey = System.getenv("TAVILY_API_KEY");
    }

    public String fetchContent(String url) {
        try {
            // Create request body
            Map<String, Object> requestBody = Map.of(
                "url", url,
                "include_images", false
            );

            // Make API call to Tavily
            String response = restClient.post()
                .uri("https://api.tavily.com/content-extraction")
                .header("Content-Type", "application/json")
                .header("x-api-key", apiKey)
                .body(requestBody)
                .retrieve()
                .body(String.class);

            // Parse the JSON response
            JsonNode root = objectMapper.readTree(response);
            return root.path("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch content from " + url, e);
        }
    }
}
```

Now create a test that uses this fetched content with prompt stuffing:

```java
@Test
void promptStuffingWithExternalContent() {
    ChatClient chatClient = ChatClient.create(model);
    ContentFetcher contentFetcher = new ContentFetcher(new ObjectMapper());

    // Fetch content from a recent article (choose a URL with relevant content)
    String url = "https://spring.io/blog/2024/01/25/spring-ai-in-action";
    String articleContent = contentFetcher.fetchContent(url);

    // Create a prompt that includes the article content
    String prompt = """
            I'll provide you with an article about Spring AI. Please summarize the key features
            and use cases mentioned in the article in 5 bullet points.

            Here's the article:
            %s

            Provide only the summary bullets in your response.
            """.formatted(articleContent);

    // Send the prompt to the model
    String response = chatClient.prompt()
            .user(prompt)
            .call()
            .content();

    System.out.println("Summary from prompt stuffing:");
    System.out.println(response);

    // Ensure we got a response
    assertFalse(response.isEmpty());
    assertTrue(response.contains("•") || response.contains("-"),
            "Response should contain bullet points");
}
```

### 11.2 Retrieval-Augmented Generation (RAG)

In this lab, you'll implement a basic RAG system using Spring AI's vector store capabilities.

First, add the necessary vector store dependencies to your build.gradle.kts:

```kotlin
dependencies {
    // Existing dependencies

    // For vector store operations
    implementation("org.springframework.ai:spring-ai-pgvector-store")
    // For in-memory vector operations (SimpleVectorStore)
    implementation("org.springframework.ai:spring-ai-core")
    // OpenAI embedding model
    implementation("org.springframework.ai:spring-ai-openai-spring-boot-starter") // Already included
}
```

Now create a class to handle the RAG process:

```java
@Component
public class RAGService {
    private final ChatClient chatClient;
    private final ContentFetcher contentFetcher;
    private final OpenAiEmbeddingModel embeddingModel;
    private final SimpleVectorStore vectorStore;

    public RAGService(
            OpenAiChatModel chatModel,
            OpenAiEmbeddingModel embeddingModel,
            ContentFetcher contentFetcher) {
        this.chatClient = ChatClient.create(chatModel);
        this.embeddingModel = embeddingModel;
        this.contentFetcher = contentFetcher;
        this.vectorStore = new SimpleVectorStore(embeddingModel);
    }

    public void addContentToKnowledgeBase(String url, String source) {
        // Fetch content
        String content = contentFetcher.fetchContent(url);

        // Split content into chunks
        List<String> chunks = splitTextIntoChunks(content, 500, 100);

        // Create documents with metadata
        List<Document> documents = chunks.stream()
                .map(chunk -> new Document(chunk, Map.of("source", source, "url", url)))
                .toList();

        // Add documents to vector store
        vectorStore.add(documents);
    }

    public String query(String question, int topK) {
        // Search for relevant documents
        List<Document> relevantDocs = vectorStore.similaritySearch(question, topK);

        // Construct a prompt with the retrieved documents
        StringBuilder contextBuilder = new StringBuilder();
        for (Document doc : relevantDocs) {
            contextBuilder.append("Source: ").append(doc.getMetadata().get("source")).append("\n");
            contextBuilder.append(doc.getContent()).append("\n\n");
        }

        // Create the final prompt
        String prompt = """
                Answer the following question based ONLY on the provided context:

                Context:
                %s

                Question: %s

                If the context doesn't contain relevant information, say "I don't have enough information to answer this question."
                """.formatted(contextBuilder, question);

        // Get the response from the LLM
        return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
    }

    private List<String> splitTextIntoChunks(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = text.split("\\. ");

        StringBuilder currentChunk = new StringBuilder();
        for (String sentence : sentences) {
            // If adding this sentence would exceed chunk size, store current chunk and start a new one
            if (currentChunk.length() + sentence.length() > chunkSize && currentChunk.length() > 0) {
                chunks.add(currentChunk.toString());

                // Keep some overlap for context continuity
                String[] words = currentChunk.toString().split("\\s+");
                currentChunk = new StringBuilder();
                if (words.length > overlap) {
                    for (int i = words.length - overlap; i < words.length; i++) {
                        currentChunk.append(words[i]).append(" ");
                    }
                }
            }

            currentChunk.append(sentence).append(". ");
        }

        // Add the final chunk if it's not empty
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }

        return chunks;
    }
}
```

Now create a test that uses the RAG service:

```java
@Test
void retrievalAugmentedGeneration() {
    // Create the RAG service
    RAGService ragService = new RAGService(model, embeddingModel, new ContentFetcher(new ObjectMapper()));

    // Add content to the knowledge base
    ragService.addContentToKnowledgeBase(
            "https://spring.io/blog/2024/01/25/spring-ai-in-action",
            "Spring AI Blog");

    // Add more content if needed
    ragService.addContentToKnowledgeBase(
            "https://spring.io/blog/2024/03/11/spring-ai-at-spring-one-essentials",
            "Spring One Blog");

    // Query the RAG system
    String question = "What are the key features of Spring AI?";
    String response = ragService.query(question, 3);

    System.out.println("RAG Response:");
    System.out.println(response);

    // Test with a question that is out of scope
    String outOfScopeQuestion = "How do I implement OAuth2 in Spring Security?";
    String outOfScopeResponse = ragService.query(outOfScopeQuestion, 3);

    System.out.println("\nOut of scope RAG Response:");
    System.out.println(outOfScopeResponse);

    // Assertions
    assertFalse(response.isEmpty());
    assertTrue(outOfScopeResponse.contains("don't have enough information") ||
               outOfScopeResponse.contains("not enough information"),
               "Should indicate lack of information for out-of-scope questions");
}
```

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