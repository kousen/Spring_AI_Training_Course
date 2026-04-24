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
2. **Spring Boot 3.5.9** with **Spring AI 1.1.4**
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

# Build the project
./gradlew build

# Run basic tests (many will be empty until you implement them)
./gradlew test

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

## Learning Approach

- **Start with TODOs**: Each test class contains guided TODO comments
- **Build incrementally**: Complete one lab before moving to the next
- **Reference solutions**: Check the `solutions` branch when needed
- **Hands-on learning**: Learn by implementing, not copying

## Version Policy

This course is pinned to Spring AI 1.1.4 for Spring Boot 3.5.x stability. Spring AI 2.0 is currently available as a milestone release and is not used in the Monday delivery labs.

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

### Production-Ready Patterns
- **Profile-based Configuration**: Separate concerns with Spring profiles
- **Primary ChatModel**: Resolve multiple AI provider ambiguity  
- **Service Layer Architecture**: Proper separation of concerns
- **Comprehensive Testing**: Unit and integration tests with modern `@MockitoBean`
- **Error Handling**: Retries, circuit breakers, and graceful degradation
- **Cost Optimization**: Token management and model selection strategies
- **Security & Observability**: API key management, monitoring, and tracing

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
