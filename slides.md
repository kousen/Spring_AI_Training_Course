---
theme: seriph
background: https://source.unsplash.com/1920x1080/?ai,technology
class: text-center
highlighter: shiki
lineNumbers: true
info: |
  ## Spring AI Training Course
  
  By Kenneth Kousen
  
  A comprehensive hands-on training course for building intelligent applications with Spring AI
drawings:
  persist: false
transition: slide-left
title: "Spring AI: Building Intelligent Applications"
mdc: true
---

# Spring AI: Building Intelligent Applications

<div class="pt-12">
  <span @click="$slidev.nav.next" class="px-2 py-1 rounded cursor-pointer" hover="bg-white bg-opacity-10">
    From basic chat to advanced RAG and MCP <carbon:arrow-right class="inline"/>
  </span>
</div>

<div class="abs-br m-6 flex gap-2">
  <button @click="$slidev.nav.openInEditor()" title="Open in Editor" class="text-xl slidev-icon-btn opacity-50 !border-none !hover:text-white">
    <carbon:edit />
  </button>
  <a href="https://github.com/kousen/Spring_AI_Training_Course" target="_blank" alt="GitHub" title="Open in GitHub"
    class="text-xl slidev-icon-btn opacity-50 !border-none !hover:text-white">
    <carbon-logo-github />
  </a>
</div>

---

# Contact Info

Ken Kousen<br>
Kousen IT, Inc.

- ken.kousen@kousenit.com
- http://www.kousenit.com
- http://kousenit.org (blog)
- Social Media:
  - [@kenkousen](https://twitter.com/kenkousen) (Twitter)
  - [@kousenit.com](https://bsky.app/profile/kousenit.com) (Bluesky)
  - [https://www.linkedin.com/in/kenkousen/](https://www.linkedin.com/in/kenkousen/) (LinkedIn)
- *Tales from the jar side* (free newsletter)
  - https://kenkousen.substack.com
  - https://youtube.com/@talesfromthejarside

---
layout: two-cols
---

# What You'll Learn

<v-clicks>

- **Basic AI Integration**: ChatClient fundamentals
- **Streaming Responses**: Real-time AI interactions
- **Structured Data**: AI-powered object extraction
- **Multimodal AI**: Vision and audio capabilities
- **Function Calling**: Extend AI with custom tools
- **RAG Systems**: Knowledge-augmented AI
- **MCP Protocol**: Model Context Protocol implementation
- **Production Patterns**: Enterprise-ready architectures

</v-clicks>

::right::

<div class="mt-8">
<img src="https://images.unsplash.com/photo-1677442136019-21780ecad995?w=350&h=400&fit=crop&brightness=1.2" alt="AI and Spring" class="rounded-lg opacity-80" />
</div>

<!-- Presenter notes: Emphasize hands-on nature, 16 progressive labs -->

---

# Repository Structure

```bash
Spring_AI_Training_Course/
├── labs.md             # 16 progressive lab exercises
├── src/
│   ├── main/java/      # Service implementations
│   ├── main/resources/ # Configuration & templates
│   └── test/java/      # Test-driven exercises
├── README.md           # Course documentation
└── slides.md           # This presentation
```

<v-clicks>

- **Start**: labs.md walks you through every step
- **Reference**: the repo contains the complete working code
- **Learn by doing**: Implement each lab incrementally
- **16 Labs**: From basic chat to an agents capstone

</v-clicks>

---

# Teaching Path

<div class="grid grid-cols-3 gap-6">

<div>

## Core

- Labs 1-6
- Lab 10
- Lab 12

</div>

<div>

## Demo

- Labs 7-9
- Multimodal APIs
- Audio/image costs

</div>

<div>

## Advanced

- Lab 13 Redis
- Labs 14-15 MCP
- Lab 16 Agents

</div>

</div>

---

# Spring AI Ecosystem

<div class="grid grid-cols-3 gap-6">

<div>

## **AI Providers**

<v-clicks>

- OpenAI (GPT, DALL-E)
- Anthropic (Claude)
- Azure OpenAI
- Google Vertex AI
- Local models (Ollama)

</v-clicks>

</div>

<div>

## **Vector Stores**

<v-clicks>

- SimpleVectorStore (in-memory)
- Redis Vector Store  
- Pinecone, Weaviate
- PgVector, Chroma

</v-clicks>

</div>

<div>

## **Capabilities**

<v-clicks>

- Text generation
- Image analysis/generation
- Speech-to-text/text-to-speech
- Function calling
- RAG workflows

</v-clicks>

</div>

</div>

---

# Prerequisites

<div class="grid grid-cols-2 gap-8">

<div>

## Technical Requirements

<v-clicks>

- **Java 17+**
- **Spring Boot 4.1.0**
- **Spring AI 2.0.0**
- **Git**
- **Redis** (optional, for advanced RAG)

</v-clicks>

</div>

<div>

## Environment Setup

```bash
# Required API key
export OPENAI_API_KEY=your_key

# Local models: install Ollama, then
ollama pull gemma4

# Optional: Redis for advanced labs
docker run -p 6379:6379 redis/redis-stack:latest

# Clone and start
git clone <repo-url>
./scripts/check-course-env
./gradlew compileJava compileTestJava
```

</div>

</div>

---
layout: section
---

# Lab 1-3: Foundations
## Building Your First AI-Powered Spring Application

---

# Lab 1: Basic Chat Interactions

````md magic-move
```java
// TODO: Create your first ChatClient
@Service
public class ChatService {
    // What goes here?
}
```

```java
// Step 1: Add ChatClient dependency
@Service
public class ChatService {
    private final ChatClient chatClient;
    
    // TODO: How do we use it?
}
```

```java
// Step 2: Complete implementation
@Service
public class ChatService {
    private final ChatClient chatClient;
    
    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }
    
    public String generateResponse(String prompt) {
        return chatClient.prompt(prompt)
            .call()
            .content();
    }
}
```
````

<v-click>

**Result**: AI-powered responses in your Spring application! 🎉

</v-click>

<!-- Presenter notes: Show live demo, explain auto-configuration -->

---

# Lab 2: Request/Response Logging

```java {1-5|7-12|14-18}
@Service
public class ChatService {
    private final ChatClient chatClient;
    
    public ChatService(ChatClient.Builder builder) {
        
        // Add logging advisor for debugging
        this.chatClient = builder
            .defaultAdvisors(new SimpleLoggerAdvisor())
            .build();
    }
    
    public String generateResponse(String prompt) {
        return chatClient.prompt(prompt)
            .call()
            .content();
    }
}
```

<v-click>

**Debug Output**: See exactly what's sent to and received from AI models

</v-click>

---

# Lab 3: Streaming Responses

```java {all|5-8|10-13}
@RestController
public class ChatController {
    
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam String message) {
        return chatClient.prompt(message)
            .stream()
            .content();
    }
    
    // Frontend receives real-time token-by-token responses
    // Perfect for chat interfaces and long AI responses
    // Uses Spring WebFlux Reactor streams
}
```

<v-click>

**Experience**: Real-time AI responses like ChatGPT interface

</v-click>

---
layout: section
---

# Lab 4-6: Structured AI
## From Text to Objects

---

# Lab 4: Structured Data Extraction

```java {1-4|6-10|12-16}
// Define your data structure
public record ActorFilms(String actor, List<String> movies) {}

@Test
void shouldGetActorFilms() {
    // AI converts natural language to structured data
    ActorFilms actorFilms = chatClient.prompt("Generate the filmography for Tom Hanks")
        .call()
        .entity(ActorFilms.class);
    
    // Assert AI returned proper structure
    assertThat(actorFilms.actor()).isEqualTo("Tom Hanks");
    assertThat(actorFilms.movies()).contains("Forrest Gump", "Cast Away");
}
```

<v-click>

**Magic**: AI understands your Java objects and populates them correctly!

</v-click>

---

# Lab 5: Prompt Templates

```java {1-6|8-14|16-20}
@Component
public class TemplateService {
    
    @Value("classpath:/prompts/actor-filmography.st")
    private Resource actorFilmographyTemplate;
    
    public ActorFilms getActorFilms(String actorName) {
        return chatClient.prompt()
            .user(userSpec -> userSpec
                .text(actorFilmographyTemplate)
                .param("actor", actorName)
                .param("count", 5))
            .call()
            .entity(ActorFilms.class);
    }
}
```

**Template File** (`actor-filmography.st`):
```
Generate a filmography for {actor}. 
Include exactly {count} of their most famous movies.
Format as JSON with actor name and movies array.
```

<!-- Presenter notes: Show StringTemplate syntax, parameter substitution -->

---

# Lab 6: Chat Memory

```java {1-9|11-19|21-25}
@Service
public class ConversationService {
    private static final String CONVERSATION_ID = "course-demo";
    private final ChatClient chatClient;
    
    public ConversationService(ChatClient.Builder builder, ChatMemory memory) {
        this.chatClient = builder
            .defaultAdvisors(MessageChatMemoryAdvisor.builder(
                memory).build())
            .build();
    }
    
    public String continueConversation(String message) {
        return chatClient.prompt()
            .advisors(a -> a.param(
                ChatMemory.CONVERSATION_ID, CONVERSATION_ID))
            .user(message)
            .call()
            .content();
        // AI remembers previous messages in this conversation!
    }
}
```

---

# Lab 6: Memory in Action

<v-click>

**Try this**: 
1. "My name is John"
2. "What's my name?" → "Your name is John"

</v-click>

---
layout: section
---

# Lab 7-9: Multimodal AI
## Beyond Text: Vision and Audio

`RUN_MULTIMODAL_TESTS=true ./gradlew test --tests OpenAiTests --tests AudioTests`

---

# Lab 7: Vision Capabilities

```java {1-6|8-14|16-20}
@Test
void shouldAnalyzeImage() {
    var imageResource = new ClassPathResource(
        "/images/multimodal_test_image.png");
    
    String response = chatClient.prompt()
        .user(userSpec -> userSpec
            .text("What do you see in this image?")
            .media(MimeTypeUtils.IMAGE_PNG, imageResource))
        .call()
        .content();
    
    assertThat(response.toLowerCase())
        .contains("dog", "playing");
}
```

---

# Vision: What AI Can Analyze

<div class="grid grid-cols-2 gap-8">

<div>

<v-clicks>

- **Photos and diagrams**
- **Charts and graphs** 
- **Screenshots and UI mockups**
- **Medical images**
- **Technical drawings**

</v-clicks>

</div>

<div class="mt-4">
<img :src="'/images/ai-vision-dog.jpg'" alt="AI Vision" class="rounded-lg opacity-80" />
</div>

</div>

---

# Lab 8: Image Generation

```java {1-8|10-16|18-22}
@Service
public class ImageService {
    private final ImageModel imageModel;
    
    public ImageService(ImageModel imageModel) {
        this.imageModel = imageModel;
    }
    
    public String generateImage(String prompt) {
        ImageResponse response = imageModel.call(
            new ImagePrompt(prompt,
                OpenAiImageOptions.builder()
                    .model("gpt-image-1")
                    .build()));

        // gpt-image-1 returns base64-encoded images
        return response.getResult()
            .getOutput()
            .getB64Json();
    }
}
```

<v-click>

**Create**: AI-generated images from text descriptions

</v-click>

---

# Lab 9: Audio Capabilities

```java {1-8|10-16|18-22}
@Service
public class SpeechService {
    private final OpenAiAudioSpeechModel speechModel;

    public SpeechService(OpenAiAudioSpeechModel speechModel) {
        this.speechModel = speechModel;
    }

    public byte[] generateSpeech(String text) {
        var options = OpenAiAudioSpeechOptions.builder()
            .voice(OpenAiAudioSpeechOptions.Voice.ALLOY)
            .responseFormat(OpenAiAudioSpeechOptions
                .AudioResponseFormat.MP3)
            .speed(1.0)
            .build();

        var prompt = new TextToSpeechPrompt(text, options);
        return speechModel.call(prompt).getResult().getOutput();
    }
}
```

<v-click>

**Output**: High-quality AI-generated speech from text

</v-click>

---

# Lab 9: Speech-to-Text

```java {1-8|10-16|18-22}
@Service
public class AudioService {
    private final AudioTranscriptionModel transcriptionModel;
    
    public AudioService(AudioTranscriptionModel model) {
        this.transcriptionModel = model;
    }
    
    public String transcribeAudio(Resource audioFile) {
        var prompt = new AudioTranscriptionPrompt(audioFile);
        var response = transcriptionModel.call(prompt);
        return response.getResult().getOutput();
    }
}
```

<v-click>

**Capability**: Convert speech files (MP3, WAV) to accurate text transcription

</v-click>

---
layout: section
---

# Lab 10-11: Application Patterns
## Tools and Production Refactoring

---

# Lab 10: AI Tools (Function Calling)

````md magic-move
```java
// Step 1: Create a tool
@Component
public class DateTimeTools {
    @Tool(description = "Get the current date and time")
    public String getCurrentDateTime() {
        return LocalDateTime.now().toString();
    }
}
```

```java
// Step 2: Register with ChatClient
@Service
public class ToolEnabledService {
    private final ChatClient chatClient;
    
    public ToolEnabledService(ChatModel model) {
        this.chatClient = ChatClient.create(model);
    }
}
```

```java
// Step 3: AI automatically calls your tools
@Test
void shouldCallTool() {
    String response = chatClient.prompt()
        .user("What time is it right now?")
        .tools(new DateTimeTools())
        .call()
        .content();
    
    // AI called getCurrentDateTime() automatically!
    assertThat(response).contains("2024");
}
```
````

**Result**: AI can execute your Java methods when needed!

---

# New in 2.0: Smarter Advisors

<div class="grid grid-cols-2 gap-4">

<div>

## Self-Correcting Output

```java
var advisor = StructuredOutputValidationAdvisor
    .builder()
    .outputType(ActorFilms.class)
    .maxRepeatAttempts(2)
    .build();
```

<v-click>

Bad JSON? The advisor sends the schema error back to the model to repair

</v-click>

</div>

<div>

## Tool Search at Scale

```java
var advisor = ToolSearchToolCallingAdvisor
    .builder()
    .toolIndex(new RegexToolIndex())
    .maxResults(3)
    .build();
```

<v-click>

Model gets one *search* tool, pulls in only what each query needs — Spring reports 34-64% token savings on large tool sets

</v-click>

</div>

</div>

<!-- Presenter notes:
- Both are Spring AI 2.0 features; see Lab 4 and Lab 10 bonus sections
- Tool search matters most with multiple MCP servers connected (hundreds of tool definitions)
- Tool search advisor needs a conversation id param (per-session index) and its own starter dependency
-->

---

# Lab 11: Refactoring for Production

```java {1-8|10-18|20-27}
@Service
public class FilmographyService {
    private final ChatClient chatClient;

    public FilmographyService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    public List<ActorFilms> getFilmography(String... actors) {
        String actorList = String.join(" and ", actors);
        return chatClient.prompt()
            .user("Generate 5 movies for " + actorList)
            .call()
            .entity(new ParameterizedTypeReference<>() {});
    }
}

@RestController
@RequestMapping("/api/films")
class FilmographyController {
    private final FilmographyService service;
    // constructor and @GetMapping methods
}
```

---
layout: section
---

# Lab 12-13: RAG Systems
## Knowledge-Augmented AI

---

# Lab 12: The RAG Problem

<div class="grid grid-cols-2 gap-8">

<div>

## Traditional AI Limitations

<v-clicks>

- Knowledge cutoff dates
- Can't access your documents  
- No real-time information
- Generic responses only

</v-clicks>

</div>

<div>

## RAG Solution

```mermaid {scale: 0.8}
graph TD
    A[Question] --> B[Vector Search]
    B --> C[Context + Question]
    C --> D[AI Model]
    D --> E[Enhanced Answer]
```

</div>

</div>

---

# RAG: How It Works

<v-clicks>

1. **Document Processing**: Split documents into chunks
2. **Embedding Generation**: Convert chunks to vectors  
3. **Vector Storage**: Store in searchable database
4. **Query Processing**: Find relevant chunks for question
5. **Context Enhancement**: Add found content to AI prompt
6. **Enhanced Response**: AI answers using your data

</v-clicks>

<v-click>

**Result**: AI answers using YOUR documents and data! 🎯

</v-click>

---

# Spring AI: Supported Providers

<div class="grid grid-cols-3 gap-6">

<div>

## **Chat Models**

<v-clicks>

- OpenAI • Azure OpenAI
- Anthropic • Google VertexAI
- Amazon Bedrock • Ollama
- Hugging Face • Mistral AI
- Groq • NVIDIA • Perplexity
- DeepSeek • Moonshot AI
- QianFan • ZhiPu AI • MiniMax

</v-clicks>

</div>

<div>

## **Embedding Models**

<v-clicks>

- OpenAI • Azure OpenAI
- Amazon Bedrock • VertexAI
- Ollama • Mistral AI
- PostgresML • ONNX
- QianFan • ZhiPu AI
- OCI GenAI • MiniMax

</v-clicks>

</div>

<div>

## **Image & Audio**

<v-clicks>

- **Images**: OpenAI DALL-E
- **Images**: Stability AI, ZhiPu AI
- **Speech-to-Text**: OpenAI Whisper
- **Text-to-Speech**: OpenAI TTS
- **Moderation**: OpenAI, Mistral

</v-clicks>

</div>

</div>

**Spring AI advantage**: Portable API - switch providers with configuration only!

---

# Understanding Embeddings & Vector Search

<div class="grid grid-cols-2 gap-8">

<div>

## **What are Embeddings?**

<v-clicks>

- **Numerical representation** of text meaning
- **High-dimensional vectors** (typically 1536+ dimensions)
- **Semantic similarity** via distance calculations
- **Context-aware** - same words, different meanings

</v-clicks>

</div>

<div>

## **Chunking Strategies**

<v-clicks>

- **Token-based**: Split by token count (GPT tokenizer)
- **Sentence-based**: Preserve sentence boundaries
- **Semantic**: Split by topic/meaning changes
- **Overlapping**: Chunks share context at boundaries

</v-clicks>

</div>

</div>

---

# RAG Implementation

````md magic-move
```java
// Step 1: Document Processing
@Service
public class DocumentProcessor {
    public void loadDocuments(VectorStore vectorStore) {
        // Load PDF documents
        PagePdfDocumentReader pdfReader = 
            new PagePdfDocumentReader("classpath:/docs/spring-ai-reference.pdf");
        
        // Split into chunks
        TextSplitter splitter = TokenTextSplitter.builder().build();
        List<Document> documents = splitter.apply(pdfReader.get());
        
        // Store as vectors
        vectorStore.add(documents);
    }
}
```

```java
// Step 2: RAG Service
@Service
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    
    public String askQuestion(String question) {
        return chatClient.prompt()
            .advisors(QuestionAnswerAdvisor.builder(vectorStore).build())
            .user(question)
            .call()
            .content();
    }
}
```

```java
// Step 3: Testing RAG
@Test
void shouldAnswerFromDocuments() {
    String answer = ragService.askQuestion(
        "What is Spring AI ChatClient?");
    
    // AI uses loaded PDF content to answer!
    assertThat(answer).contains("ChatClient", "Spring AI");
}
```
````

**Magic**: AI answers questions using your PDF documents!

---

# Lab 13: Production RAG with Redis

```java {1-8|10-14}
@Configuration
@Profile("redis")  
public class RedisRAGConfig {
    
    @Bean
    public VectorStore vectorStore(RedisConnectionFactory factory) {
        return new RedisVectorStore(factory, embeddingModel());
    }
    
    @Bean
    public ApplicationRunner dataLoader(VectorStore vectorStore) {
        return args -> loadDocumentsIfEmpty(vectorStore);
    }
}
```

---

# Redis RAG: Smart Data Loading

```java
@Bean
public ApplicationRunner dataLoader(VectorStore vectorStore) {
    return args -> {
        if (vectorStore instanceof RedisVectorStore redis && 
            redis.getCollection().isEmpty()) {
            
            // Only load documents if Redis is empty
            loadDocuments(vectorStore);
            log.info("Loaded {} documents into Redis", count);
        }
    };
}
```

**Benefits**: Persistent • Scalable • Fast similarity search

---
layout: section
---

# Lab 14-15: Model Context Protocol
## The Future of AI Tool Integration

---
layout: two-cols
---

# What is MCP?

<v-clicks>

- **Standardized protocol** for AI tool communication
- **Open source** by Anthropic
- **Universal interface** between AI and tools
- **Secure sandbox** for AI operations
- **Growing ecosystem** of MCP servers

</v-clicks>

::right::

```mermaid
graph TB
    A[AI Model] --> B[MCP Protocol]
    B --> C[File System]
    B --> D[Database]
    B --> E[Web APIs]
    B --> F[Custom Tools]
    B --> G[Search Engine]
```

<div class="mt-4 text-sm opacity-75">
MCP enables AI to securely access external tools and data sources
</div>

---

# Lab 14: MCP Client

```java {1-6|7-15}
@SpringBootTest
@ActiveProfiles("mcp")
@EnabledIfEnvironmentVariable(
    named = "RUN_MCP_CLIENT_TESTS",
    matches = "true")
public class McpClientTests {
    @Autowired
    private ChatModel chatModel;

    @Autowired(required = false)
    private ToolCallbackProvider toolCallbackProvider;  // MCP tool provider

    private ChatClient chatClient;
    private ToolCallback[] mcpTools;
    // setUp() on next slide...
}
```

---

# Lab 14: MCP Client — Setup

```java {1-6|7-11}
@BeforeEach
void setUp() {
    // Extract tools from the provider
    if (toolCallbackProvider != null) {
        mcpTools = toolCallbackProvider.getToolCallbacks();
    }
    // Build ChatClient with discovered tools
    chatClient = ChatClient.builder(chatModel)
        .defaultToolCallbacks(mcpTools)
        .build();
}
```

---

# MCP Client: Configuration

**Configuration** (`application-mcp.properties`):
```properties
# Enable MCP client
spring.ai.mcp.client.enabled=true
spring.ai.mcp.client.toolcallback.enabled=true

# Context7 - Library documentation lookup
spring.ai.mcp.client.stdio.connections.context7.command=npx
spring.ai.mcp.client.stdio.connections.context7.args=-y,@upstash/context7-mcp@latest

# Tavily - AI-optimized web search (requires TAVILY_API_KEY)
spring.ai.mcp.client.stdio.connections.tavily.command=npx
spring.ai.mcp.client.stdio.connections.tavily.args=-y,tavily-mcp@latest
```

**Result**: 6 tools discovered - 2 from Context7, 4 from Tavily!

---

# Lab 15: MCP Server

````md magic-move
```java
// Step 1: Create tools with @Tool annotation
@Service
public class CalculatorService {
    @Tool(description = "Add two numbers together")
    public double add(double a, double b) {
        return a + b;
    }
    
    @Tool(description = "Calculate compound interest")
    public double calculateCompoundInterest(
        double principal, double rate, int years) {
        return principal * Math.pow(1 + rate, years);
    }
}
```

```java
// Step 2: Spring AI auto-exposes as MCP server
@Configuration
@Profile("mcp-server")
public class McpServerConfig {
    // No explicit configuration needed!
    // @Tool annotated methods are automatically discovered
    
    @Bean
    public ApplicationRunner mcpServerLogger() {
        return args -> {
            System.out.println("MCP Server started with tools:");
            System.out.println("  • add(double, double) - Add numbers");
            System.out.println("  • calculateCompoundInterest(...) - Finance");
        };
    }
}
```

```bash
# Step 3: Run MCP server
./gradlew bootRun --args='--spring.profiles.active=mcp-server'

# Step 4: Connect from Claude Desktop
# Add to Claude's MCP config:
{
  "mcpServers": {
    "spring-calculator": {
      "command": "java",
      "args": ["-jar", "app.jar", "--spring.profiles.active=mcp-server"]
    }
  }
}
```
````

**Result**: Claude Desktop can use your Java methods as tools!

---

# MCP in Production: Security

<v-clicks>

- **STDIO transport sidesteps auth** — the client launches the server as a child process; OS process ownership *is* the trust boundary
- **HTTP transports need real auth** — streamable HTTP servers are network services; the MCP spec prescribes **OAuth 2.1** (resource server pattern)
- **Spring support**: [mcp-security](https://github.com/spring-ai-community/mcp-security) (Spring AI Community) integrates MCP servers with Spring Security
- **Rule of thumb**: local dev tools → STDIO; anything shared or deployed → streamable HTTP + OAuth

</v-clicks>

<!-- Presenter notes:
- This is the #1 question after any MCP demo: "how do I secure this?"
- Demo anchor: MockHub project uses mcp-security for real-world MCP auth
- The calculator lab uses STDIO, which is why no auth appears in the config
-->

---
layout: section
---

# Production Patterns
## Enterprise-Ready Spring AI

---

# Service Layer: REST Controller

```java {1-6|8-16}
@RestController
@RequestMapping("/api/ai")
public class AIController {
    private final ChatService chatService;
    
    @PostMapping("/chat")
    public ResponseEntity<String> chat(@RequestBody ChatRequest request) {
        try {
            String response = chatService.generateResponse(request.getMessage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body("AI service temporarily unavailable");
        }
    }
```

---

# Service Layer: Streaming Support

```java {1-8|10-14}
@GetMapping("/chat/stream")
public ResponseEntity<Flux<String>> streamChat(@RequestParam String message) {
    Flux<String> stream = chatService.streamResponse(message)
        .onErrorReturn("Error occurred during streaming");
        
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_EVENT_STREAM)
        .body(stream);
}

// Clean separation of concerns
// Proper error handling
// Stream-ready architecture
// Production-ready patterns
```

---

# Configuration Management

```java {1-8|10-16}
@Configuration
public class AIConfiguration {
    
    @Bean
    @Primary
    @ConditionalOnProperty("spring.ai.openai.api-key")
    public ChatModel primaryChatModel(OpenAiChatModel openAiModel) {
        return openAiModel;
    }
    
    @Bean
    @Profile("rag")
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return SimpleVectorStore.builder(embeddingModel).build();
    }
}
```

---

# Profile-Based Feature Activation

```java
@Bean
@Profile({"rag", "redis"})
public VectorStore redisVectorStore(RedisConnectionFactory factory) {
    return new RedisVectorStore(factory, embeddingModel());
}

@Bean
@Profile("mcp")
public McpClientConfiguration mcpConfig() {
    return new McpClientConfiguration();
}
```

**Run configurations**:
```bash
./gradlew bootRun                                    # Basic AI
./gradlew bootRun --args='--spring.profiles.active=rag,redis'  # RAG
./gradlew bootRun --args='--spring.profiles.active=mcp'        # MCP
```

---

# Error Handling: Retries

```java {1-8|10-14}
@Service
public class ResilientChatService {
    private final ChatClient chatClient;
    
    @Retryable(value = {ApiException.class}, maxAttempts = 3)
    public String generateResponse(String prompt) {
        return chatClient.prompt(prompt).call().content();
    }
    
    // Automatically retries API failures
    // Exponential backoff available
    // Works with Spring Retry
}
```

---

# Error Handling: Circuit Breaker

```java {1-6|8-12}
@CircuitBreaker(name = "ai-service", fallbackMethod = "fallbackResponse")
public String generateResponseWithCircuitBreaker(String prompt) {
    return chatClient.prompt(prompt).call().content();
}

public String fallbackResponse(String prompt, Exception ex) {
    log.warn("AI service failed, using fallback", ex);
    return "I'm temporarily unable to process your request.";
}

// Prevents cascade failures
// Automatic recovery when service improves
```

---

# Error Handling: Async Processing

```java
@Async
public CompletableFuture<String> generateResponseAsync(String prompt) {
    return CompletableFuture.supplyAsync(() -> 
        chatClient.prompt(prompt).call().content());
}

// Usage
CompletableFuture<String> future = service.generateResponseAsync("Hello");
String result = future.get(30, TimeUnit.SECONDS);
```

**Benefits**: Non-blocking • Timeout control • Better UX

---

# Testing: Unit Tests with Mocks

```java {1-6|8-14}
@SpringBootTest
class ChatServiceTest {
    @MockitoBean ChatClient chatClient;
    @MockitoBean ChatClient.CallResponseSpec callSpec;
    
    @Test
    void shouldGenerateResponse() {
        when(chatClient.prompt("Hello")).thenReturn(callSpec);
        when(callSpec.call()).thenReturn(mockResponse("Hi there!"));
        
        String result = service.generateResponse("Hello");
        assertThat(result).isEqualTo("Hi there!");
    }
}
```

---

# Testing: Integration with TestContainers

```java {1-6|8-12}
@Testcontainers
@SpringBootTest
class RAGIntegrationTest {
    
    @Container
    static RedisContainer redis = new RedisContainer("redis:7.0")
        .withExposedPorts(6379);
        
    @Test
    void shouldPerformRAGWithRedis() {
        // Test actual RAG workflow with real Redis
        String answer = ragService.askQuestion("What is Spring AI?");
        assertThat(answer).contains("Spring", "AI");
    }
}
```

**Benefits**: Real dependencies • Isolated environments • CI/CD ready

---

# Cost & Performance Optimization

<div class="grid grid-cols-2 gap-8">

<div>

## **Model Selection**

<v-clicks>

- **Small models**: Fast, cheap, great for routing
- **Reasoning models**: Slower, better for complex tasks
- **Multimodal models**: Images, audio, documents
- **Local models**: Privacy, offline-friendly

</v-clicks>

</div>

<div>

## **Optimization Strategies**

<v-clicks>

- **Token Management**: Monitor usage, optimize prompts
- **Embedding Caching**: Store frequently used vectors
- **Request Batching**: Combine operations when possible
- **Smart Chunking**: Optimize document splitting

</v-clicks>

</div>

</div>

---

# Security & Observability

<div class="grid grid-cols-2 gap-8">

<div>

## **Security Best Practices**

<v-clicks>

- **API Key Management**: Environment variables, vaults
- **Data Privacy**: Local processing when possible
- **Input Validation**: Sanitize user prompts
- **Output Filtering**: Check AI responses

</v-clicks>

</div>

<div>

## **Monitoring & Observability**

<v-clicks>

- **Actuator + Micrometer**: Spring AI observations
- **Metrics**: Latency, token usage, vector store calls
- **Tracing**: ChatClient, advisors, model calls
- **Prompt logging**: Useful, but off by default

</v-clicks>

</div>

</div>

---
layout: section
---

# Course Summary
## What You've Built

---

# Your AI-Powered Application Stack

<div class="grid grid-cols-2 gap-8">

<div>

## **Foundation & Advanced**

<v-clicks>

- ChatClient integration
- Multiple AI providers  
- Streaming responses
- Multimodal capabilities
- Function calling

</v-clicks>

</div>

<div>

## **Production Ready**

<v-clicks>

- RAG systems
- MCP protocol
- Service architecture
- Error handling
- Comprehensive testing

</v-clicks>

</div>

</div>

<v-click>

**Result**: Enterprise-grade Spring AI applications! 🚀

</v-click>

---

# Key Takeaways

<v-clicks>

1. **Spring AI simplifies AI integration** - No complex API calls or JSON parsing
2. **Start simple, add complexity gradually** - From basic chat to advanced RAG
3. **Leverage Spring's strengths** - Auto-configuration, profiles, testing
4. **Think beyond text** - Vision, audio, and structured data open new possibilities
5. **MCP is the future** - Standardized tool integration across AI platforms
6. **Production requires planning** - Error handling, monitoring, and resilience
7. **Test everything** - AI responses are non-deterministic but testable

</v-clicks>

---

# Next Steps: Continue Learning

<div class="grid grid-cols-2 gap-8">

<div>

## Hands-On Practice

<v-clicks>

- Explore the GitHub repository
- Try advanced RAG techniques  
- Build custom MCP servers
- Integrate with existing apps

</v-clicks>

</div>

<div>

## Key Resources

<v-clicks>

- [Spring AI Docs](https://docs.spring.io/spring-ai/reference/)
- [Model Context Protocol](https://modelcontextprotocol.io/)
- [Course Repository](https://github.com/kousen/Spring_AI_Training_Course)

</v-clicks>

</div>

</div>

---

# Production Considerations

<div class="grid grid-cols-2 gap-8">

<div>

## Operational Excellence

<v-clicks>

- **API Cost Management**: Monitor token usage
- **Rate Limiting**: Handle API quotas  
- **Data Privacy**: Keep sensitive data secure
- **Monitoring**: Track performance and errors

</v-clicks>

</div>

<div>

## Advanced Topics

<v-clicks>

- Custom embedding models
- AI agents (see next slides)
- AI-powered workflows
- Integration with existing systems

</v-clicks>

</div>

</div>

---

# AI Agents: The Landscape

<div class="grid grid-cols-2 gap-8">

<div>

## Spring AI Foundation

<v-clicks>

- **Tool calling** (`@Tool`) — you already learned this!
- **Recursive Advisors** — plan-execute-iterate loops
- **[Effective Agents Guide](https://docs.spring.io/spring-ai/reference/api/effective-agents.html)** — patterns, not a framework
- Spring AI provides *building blocks*, not a prescriptive agent framework

</v-clicks>

</div>

<div>

## Agent Frameworks & Tools

<v-clicks>

- **[Embabel](https://github.com/embabel/embabel-agent)** — Rod Johnson's agent framework *on top of* Spring AI
  - `@Agent`, `@Goal`, `@Action` annotations
  - Goal-Oriented Action Planning (from game AI)
- **[spring-ai-agent-utils](https://github.com/spring-ai-community/spring-ai-agent-utils)** — Claude Code-style tools for Spring AI (works with this course's 2.0 setup)
- **[Spring AI Agents/Bench](https://spring.io/blog/2025/10/28/agents-and-benchmarks/)** — benchmarking & evaluation

</v-clicks>

</div>

</div>

<!-- Presenter notes:
- Students already know @Tool from Lab 10 — agents build on that
- Embabel hit 1.0.0 (2026) — but 1.0 targets Boot 3.5.x/Spring AI 1.x; Boot 4 support is on its 2.0 branch, so demo from the separate OperaGenerator repo, not this project
- spring-ai-agent-utils requires Spring AI 2.0, which this course now uses — worth a live demo if time allows
- Key insight: Spring AI = Servlet API, Embabel = Spring MVC (higher abstraction)
-->

---

# Embabel: Agent Pattern in Action

The framework infers execution order from input/output types — like a type-driven workflow

````md magic-move

```java
// Step 1: Define your domain types
record UserInput(String content) {}
record Story(String text) {}
record ReviewedStory(String text, String feedback, int rating) {}
```

```java
// Step 2: Create an agent with @Action methods
@Agent(description = "Writes and reviews stories")
public class WriteAndReviewAgent {

    @Action
    public Story writeStory(UserInput input, OperationContext ctx) {
        return ctx.ai().withDefaultLlm()
            .createObject(
                "Write a story about: " + input.content(),
                Story.class);
    }

    @AchievesGoal(description = "Review a story")
    @Action
    public ReviewedStory reviewStory(Story story, OperationContext ctx) {
        return ctx.ai().withDefaultLlm()
            .createObject(
                "Review this story: " + story.text(),
                ReviewedStory.class);
    }
}
```

```java
// The framework automatically plans: UserInput → Story → ReviewedStory
// No explicit orchestration needed — types drive the workflow
//
// Compare to @Tool (Lab 10): Tools extend what an LLM can do
// Agents orchestrate multiple LLM calls toward a goal
```

````

**Demo**: [github.com/kousen/OperaGenerator](https://github.com/kousen/OperaGenerator) (`embabel` branch)

<!-- Presenter notes:
- Walk through: UserInput flows to writeStory, Story flows to reviewStory
- @AchievesGoal marks the terminal action
- Framework uses GOAP (Goal-Oriented Action Planning) to find the path
- Demo: switch to OperaGenerator repo, embabel branch, and walk through it
- Also has a langchain4j-agentic branch for comparison
- Dependency: com.embabel.agent:embabel-agent-starter-shell
- Needs OPENAI_API_KEY; verify Embabel's current Spring Boot version before the demo (OperaGenerator repo was built against Boot 3.5.x)
-->

---

# Thank You!

<div class="text-center">

## Questions?

**Kenneth Kousen**  
*Author, Speaker, Java Champion*

[kousenit.com](https://kousenit.com) | [@kenkousen](https://twitter.com/kenkousen)

<div class="mt-8 mb-8">
<img src="https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=600&h=200&fit=crop&brightness=1.2" alt="AI Future" class="rounded-lg mx-auto opacity-60" />
</div>

### Ready to build intelligent applications with Spring AI!

**Repository**: [github.com/kousen/Spring_AI_Training_Course](https://github.com/kousen/Spring_AI_Training_Course)

</div>

<!-- Final presenter notes: 
- Emphasize hands-on nature of course
- Encourage students to continue with advanced topics
- Point to repository for continued learning
- Mention that the repo contains all working code; labs.md has the step-by-step path
-->
