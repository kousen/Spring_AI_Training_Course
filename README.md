# Spring AI Training Course

A comprehensive hands-on training course for learning Spring AI through progressive lab exercises.

## Getting Started

This repository contains a complete Spring AI training course with both starter code and working implementations. Students build functionality incrementally through guided TODO exercises.

### Repository Structure

- **`main` branch**: Starter code with TODO-guided exercises
- **`solutions` branch**: Complete implementations for reference
- **`labs.md`**: Step-by-step lab instructions and exercises
- **`slides.md`**: Comprehensive Slidev presentation for training

### Prerequisites

1. **Java 17+**
2. **Spring Boot 3.5.14** with **Spring AI 1.1.7**
3. **Environment Variables**:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key
   export ANTHROPIC_API_KEY=your_anthropic_api_key  # Optional
   ```
4. **Optional: Redis** (for advanced RAG exercises):
   ```bash
   docker run -p 6379:6379 redis/redis-stack:latest
   ```

### Quick Start

```bash
# Clone the repository (main branch contains starter code)
git clone https://github.com/kousen/Spring_AI_Training_Course.git
cd Spring_AI_Training_Course

# Check your local setup
./scripts/check-course-env

# Fast verification before starting the labs
./gradlew compileJava compileTestJava
./gradlew test --tests SpringaicourseApplicationTests

# Full lab test runs may call model APIs and optional services
./gradlew test

# Optional MCP client lab: starts external npx-based MCP servers
RUN_MCP_CLIENT_TESTS=true ./gradlew test --tests McpClientTests

# Optional multimodal labs: vision, image generation, and audio APIs
RUN_MULTIMODAL_TESTS=true ./gradlew test --tests OpenAiTests --tests AudioTests

# Optional RAG evaluation pass: adds extra model calls for evaluator checks
RUN_RAG_EVALUATION_TESTS=true ./gradlew test --tests RAGTests

# View complete solutions (when needed)
git checkout solutions
```

## Course Structure

Follow the exercises in [labs.md](labs.md) to build Spring AI applications from scratch:

1. **Basic Chat Interactions** - Simple AI conversations
2. **Request/Response Logging** - Debug AI interactions
3. **Streaming Responses** - Real-time AI responses  
4. **Structured Data Extraction** - AI-powered data parsing
5. **Prompt Templates** - Reusable AI prompts
6. **Chat Memory** - Conversation context
7. **Vision Capabilities** - Image analysis
8. **Image Generation** - AI-created images
9. **Audio Processing** - Speech-to-text and text-to-speech
10. **AI Tools** - Function calling
11. **Production Refactoring** - Service and controller layers
12. **Retrieval-Augmented Generation (RAG)** - AI with knowledge base
13. **Production RAG** - Redis vector store optimization
14. **Model Context Protocol (MCP) Client** - Connect to external tool servers
15. **Model Context Protocol (MCP) Server** - Create your own tool servers

## Recommended Teaching Path

For a 3-4 hour delivery, treat Labs 1-6, 10, and 12 as the core path. They cover ChatClient, advisors, structured output, prompt templates, chat memory, tools, and a complete RAG flow.

Use Labs 7-9 as demos or short exercises depending on API access and time. Use Labs 13-15 as advanced/optional material: Redis requires a local service, and MCP is best taught after students are comfortable with tools.

## Learning Approach

- **Start with TODOs**: Each test class contains guided TODO comments
- **Build incrementally**: Complete one lab before moving to the next
- **Reference solutions**: Check the `solutions` branch when needed
- **Hands-on learning**: Learn by implementing, not copying

## Version Policy

This course is pinned to the stable Spring AI 1.1.x line for Spring Boot 3.5.x compatibility. Spring AI 2.0 milestone and release-candidate builds are preview material and are not used in the Tuesday delivery labs.

## Key Features & Technologies

### Core Spring AI Capabilities
- **Multiple AI Providers**: 18+ providers including OpenAI, Anthropic, Google VertexAI, Amazon Bedrock, Ollama, and more
- **Streaming Responses**: Real-time AI interactions with Reactor
- **Structured Data Extraction**: Convert AI responses to Java objects
- **Multimodal AI**: Vision (image analysis), Audio (speech-to-text, text-to-speech)
- **Function Calling**: Extend AI with custom tools and APIs

### Advanced Features
- **Retrieval-Augmented Generation (RAG)**: 
  - Document processing (PDF, HTML, web content)
  - Vector embeddings and similarity search
  - SimpleVectorStore (in-memory) and RedisVectorStore (persistent)
  - Smart data detection to avoid reprocessing

- **Model Context Protocol (MCP)**:
  - **MCP Client**: Connect to external tool servers (filesystem, search, etc.)
  - **MCP Server**: Expose your own tools to AI clients like Claude Desktop
  - STDIO and SSE transport support
  - Auto-discovery of @Tool annotated methods

### Production-Oriented Patterns
- **Profile-based Configuration**: Separate concerns with Spring profiles
- **Primary ChatModel**: Resolve multiple AI provider ambiguity  
- **Service Layer Architecture**: Proper separation of concerns
- **Comprehensive Testing**: Unit and integration tests with modern `@MockitoBean`
- **Error Handling**: Retries, timeouts, and graceful degradation
- **Cost Optimization**: Token management and model selection strategies
- **Security & Observability**: API key management plus Spring AI/Micrometer metrics and tracing when Actuator is enabled

## Profile Usage

The application uses Spring profiles for different features:

```bash
# Basic AI functionality (default)
./gradlew bootRun

# RAG with in-memory vector store
./gradlew bootRun --args='--spring.profiles.active=rag'

# RAG with Redis vector store (requires Redis running)
./gradlew bootRun --args='--spring.profiles.active=rag,redis'

# MCP client functionality
./gradlew bootRun --args='--spring.profiles.active=mcp'

# MCP server functionality  
./gradlew bootRun --args='--spring.profiles.active=mcp-server'
```

## Training Materials

- **Lab Instructions**: See [labs.md](labs.md) for step-by-step exercises
- **Presentation Slides**: Use [slides.md](slides.md) with Slidev for training sessions
- **Complete Examples**: Switch to `solutions` branch for working implementations
- **Issues**: Report problems via GitHub issues

### Using the Presentation

```bash
# Install Slidev globally
npm install -g @slidev/cli

# Start the presentation
slidev slides.md

# Export to PDF
slidev export slides.md
```

## License

This project is licensed under the Apache License 2.0 - see the LICENSE file for details.
