# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **hands-on training course** for learning Spring AI through progressive lab exercises. The repository is structured as a proper training course where students build functionality incrementally.

### Repository Structure

- **`main` branch**: Complete working implementations (single-branch course; starter code lives in the lab text)
- **`labs.md`**: 15 progressive lab exercises with step-by-step instructions and starter code
- **`slides.md`**: Comprehensive Slidev presentation for training sessions
- **Tags**: `spring-ai-1.1.7` and `starter-1.1.7` preserve the final Spring AI 1.x state (including the old two-branch main/solutions layout)
- **Dated branches** (`springai_aug2025`, etc.): snapshots of past class deliveries

The course demonstrates integration of Large Language Models (LLMs) with Spring applications using the Spring AI library (version 2.0.0 on Spring Boot 4.1), covering:

- Text generation and chat capabilities
- Structured data extraction  
- Prompt engineering with templates
- Chat memory for maintaining conversation context
- Vision capabilities for image understanding and generation
- Audio processing (text-to-speech and speech-to-text)
- Retrieval-Augmented Generation (RAG) with PDF and web content
- Model Context Protocol (MCP) for standardized tool integration

## Common Commands

### Build and Run

```bash
# Build the project
./gradlew build

# Run the application (default profile)
./gradlew bootRun

# Run with RAG profile enabled
./gradlew bootRun --args='--spring.profiles.active=rag'

# Run with both RAG and Redis profiles
./gradlew bootRun --args='--spring.profiles.active=rag,redis'

# Run with MCP client functionality
./gradlew bootRun --args='--spring.profiles.active=mcp'

# Run with MCP server functionality
./gradlew bootRun --args='--spring.profiles.active=mcp-server'
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test classes (students build these progressively from labs.md)
./gradlew test --tests OpenAiTests
RUN_OLLAMA_TESTS=true ./gradlew test --tests OllamaTests
./gradlew test --tests RAGTests

# Run with specific profiles (for advanced RAG exercises)
./gradlew test --tests RAGTests -Dspring.profiles.active=rag,redis

# Run MCP tests (note: may fail when run together due to profile conflicts)
./gradlew test --tests McpServerTests
RUN_MCP_CLIENT_TESTS=true ./gradlew test --tests McpClientTests
```

### Redis Setup (for RAG with Redis vector store)

```bash
# Start Redis Stack container
docker run -p 6379:6379 redis/redis-stack:latest
```

### Issue Management

```bash
# Create a new GitHub issue
gh issue create --title "Issue Title" --body "Issue description"

# List open issues
gh issue list

# Close an issue
gh issue close <issue-number>
```

**Important**: Always create GitHub issues for new features, major refactors, or bug fixes before starting work. This helps with project tracking and documentation.

**CRITICAL REMINDER**: Before implementing any significant changes or new features:
1. **CREATE** a GitHub issue first using `gh issue create`
2. **IMPLEMENT** the feature or fix
3. **CLOSE** the issue when complete using `gh issue close <number>`

This workflow ensures proper documentation and project tracking. Don't forget to close issues upon completion!

## Branch Management

This is a **single-branch course**: `main` holds the complete working implementations, and starter code lives in the lab text in `labs.md`. There is no separate solutions branch to keep in sync.

- Test methods on `main` should never contain TODO stubs — they ARE the reference implementations
- Before each class delivery, create a dated snapshot branch (e.g. `springai_oct2026`) so the delivered state is preserved
- The retired two-branch layout is preserved at tags `starter-1.1.7` (TODO stubs) and `spring-ai-1.1.7` (solutions)

## Required Environment Variables

Set these environment variables before running the application:

```bash
export OPENAI_API_KEY=your_openai_api_key
export ELEVENLABS_API_KEY=your_elevenlabs_api_key  # Optional, for the ElevenLabs TTS demo
```

For local-model exercises, install Ollama and pull a model (`ollama pull gemma4`); set `OLLAMA_MODEL` to override the default model name.

## Common Tasks

### Adding Navigation to Exercise Files

To add a table of contents with navigable links to any tutorial/exercise file:

1. Add a table of contents section at the top like this:
```markdown
## Table of Contents

- [Exercise 1: Basic Setup](#exercise-1-basic-setup)
- [Exercise 2: Advanced Features](#exercise-2-advanced-features)
```

2. For IntelliJ IDEA compatibility, use standard Markdown heading anchors (headings automatically generate anchors based on their text)

3. Add return links at the end of each section:
```markdown
[↑ Back to table of contents](#table-of-contents)
```

Note: The anchor names in the links should match the heading text (lowercase, with hyphens replacing spaces and special characters removed).

Example structure:
```markdown
## Table of Contents

- [Lab 1: Getting Started](#lab-1-getting-started)
- [Lab 2: Core Concepts](#lab-2-core-concepts)

## Lab 1: Getting Started

Content here...

[↑ Back to table of contents](#table-of-contents)

## Lab 2: Core Concepts

Content here...

[↑ Back to table of contents](#table-of-contents)
```

This pattern is useful for any long tutorial or exercise file to improve navigation.

## Code Architecture

### Key Components

1. **AI Model Clients**
   - `ChatClient` - Primary interface for interacting with AI models
   - Model-specific implementations for OpenAI (cloud), Ollama (local), and ElevenLabs (TTS)
   - `ChatModelConfig` - Resolves multiple ChatModel ambiguity with @Primary
   - Configured in `application.properties`

2. **Advisors**
   - `SimpleLoggerAdvisor` - Logs AI interactions for debugging
   - `MessageChatMemoryAdvisor` - Maintains conversation history
   - `QuestionAnswerAdvisor` - Core component for RAG workflow

3. **RAG System**
   - `VectorStore` - Stores document embeddings (Simple in-memory or Redis)
   - Document readers for various sources (PDF, HTML)
   - Text splitters for chunking documents
   - Embedding generation for semantic search

4. **MCP (Model Context Protocol)**
   - `CalculatorService` - Example MCP server with @Tool annotated methods
   - `McpServerConfig` - Configuration for MCP server functionality
   - `McpClientTests` - Demonstrates MCP client usage
   - `McpServerTests` - Tests MCP server functionality

5. **Services**
   - `RAGService` - High-level API for question answering with context

6. **Configuration**
   - `AppConfig` - Central configuration for vector stores and document processing
   - Profile-based activation of components
   - Data detection to avoid redundant processing

### Profiles

The application uses Spring profiles to enable different features:

- **Default**: Basic AI chat capabilities with 18+ supported providers (OpenAI, Anthropic, Google VertexAI, Amazon Bedrock, Ollama, etc.)
- **`rag`**: Enables Retrieval-Augmented Generation with SimpleVectorStore
- **`redis`**: Uses Redis as the vector store instead of in-memory (use with `rag`)
- **`mcp`**: Enables MCP client functionality to connect to external tool servers
- **`mcp-server`**: Enables MCP server functionality to expose tools to AI clients

## Vector Store Implementation

The project supports two vector store implementations:

1. **SimpleVectorStore** (default)
   - In-memory vector store
   - Used when the `redis` profile is not active

2. **RedisVectorStore** 
   - Persistent vector store using Redis
   - Enabled with the `redis` profile
   - Requires a running Redis Stack instance
   - Includes data detection to avoid reprocessing on restart

## MCP (Model Context Protocol) Implementation

The project includes comprehensive MCP support for both client and server scenarios:

### MCP Server
- **CalculatorService**: Exposes mathematical operations as tools via @Tool annotations
- **Auto-discovery**: Spring AI automatically discovers @Tool annotated methods
- **Multiple transports**: Supports STDIO and streamable HTTP (the MCP default since the 2025-11-25 spec)
- **Claude Desktop integration**: Ready for use with Claude Desktop MCP configuration

### MCP Client  
- **External tool integration**: Connect to filesystem, search, and other MCP servers
- **Profile-based configuration**: Clean separation via `mcp` profile
- **Multiple connections**: Support for connecting to multiple MCP servers simultaneously
- **Error handling**: Graceful handling when MCP servers are unavailable

### Configuration Files
- `application-mcp.properties`: MCP client configuration
- `application-mcp-server.properties`: MCP server configuration  
- `mcp-servers-config.json`: External server configuration example

## Training Course Structure

This is a **hands-on training course** where students implement Spring AI functionality progressively:

### Learning Approach
- **Single branch**: `main` holds the working implementations; starter code is embedded in labs.md
- **Progressive labs**: Each lab builds on previous knowledge
- **Hands-on implementation**: Students learn by coding, not copying

### Lab Progression
The course follows a structured progression documented in `labs.md` with 15 comprehensive labs:
1. **Basic chat interactions** - Simple AI conversations
2. **Request/response logging** - Debug AI interactions  
3. **Streaming responses** - Real-time AI communication
4. **Structured data extraction** - AI-powered data parsing
5. **Prompt engineering** - Template-based prompts
6. **Memory management** - Conversation context
7. **Vision capabilities** - Image analysis with AI
8. **Image generation** - AI-created images
9. **Audio processing** - Speech-to-text and text-to-speech
10. **AI Tools (Function calling)** - Extend AI with custom methods
11. **Production refactoring** - Service and controller patterns
12. **RAG implementation** - Knowledge-augmented AI
13. **Vector store optimization** - Production-ready RAG with Redis
14. **MCP client** - Connect to external tool servers
15. **MCP server** - Create your own tool servers

### Code Structure for Students
- **Test classes**: Reference implementations of every lab; students build their own versions from labs.md
- **Working examples**: DateTimeTools, ActorFilms (students use these)

## Important Notes

### Testing Considerations
- **Profile conflicts**: MCP tests may fail when run together due to Spring context caching conflicts between different profiles
- **Individual tests**: All tests pass when run individually - this is the recommended approach
- **Production usage**: Profile conflicts only affect testing, not runtime functionality

### Profile Management Best Practices
- **Default profile**: Contains only basic AI functionality (no Redis dependency)
- **Redis profile**: Only active when explicitly enabled with `redis` profile
- **MCP profiles**: Separate `mcp` and `mcp-server` profiles for clean separation
- **Multiple ChatModels**: `ChatModelConfig` provides @Primary ChatModel to resolve ambiguity

### Environment Variables
Always set required environment variables before running:
```bash
export OPENAI_API_KEY=your_openai_api_key
export ELEVENLABS_API_KEY=your_elevenlabs_api_key  # Optional
```

### Redis Requirements
For RAG with Redis (profile: `rag,redis`):
```bash
docker run -p 6379:6379 redis/redis-stack:latest
```

## Training Materials Usage

### Presentation Slides
The repository includes comprehensive Slidev presentation slides (`slides.md`) for training sessions:

```bash
# Install dependencies and start presentation mode
npm install
npm run dev

# Export to PDF locally (optional; CI publishes the PDF automatically)
npm run export
```

The slides PDF is auto-built by `.github/workflows/build-slides-pdf.yml` on every push to `main` that touches `slides.md` and published to the rolling `slides-latest` GitHub release. Do not commit exported PDFs or PPTX files.

### Presentation Features
- **15 lab progression**: Matches the complete lab sequence
- **Interactive code examples**: Magic-move animations and progressive disclosure
- **Provider overview**: Comprehensive list of 18+ supported AI providers
- **Production patterns**: Error handling, testing, cost optimization
- **Modern practices**: Updated with `@MockitoBean` and Spring Boot 4.1 patterns
- **Proper Slidev structure**: Images in `public/images/` for correct rendering

### Training Session Structure
- **Duration**: 3-4 hours with hands-on exercises
- **Format**: Progressive lab implementation with slide support
- **Materials**: Slides for concepts, labs.md for step-by-step implementation
