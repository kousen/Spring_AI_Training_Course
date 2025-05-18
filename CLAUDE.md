# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This Spring AI course project demonstrates integration of Large Language Models (LLMs) with Spring applications using the Spring AI library (version 1.0.0-M8). It showcases:

- Text generation and chat capabilities
- Structured data extraction
- Prompt engineering with templates
- Chat memory for maintaining conversation context
- Vision capabilities for image understanding and generation
- Audio processing (text-to-speech and speech-to-text)
- Retrieval-Augmented Generation (RAG) with PDF and web content

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
```

### Testing

```bash
# Run all tests
./gradlew test

# Run specific test classes
./gradlew test --tests OpenAiTests
./gradlew test --tests ClaudeTests
./gradlew test --tests RAGTests

# Run with specific profiles
./gradlew test --tests RAGTests -Dspring.profiles.active=rag,redis
```

### Redis Setup (for RAG with Redis vector store)

```bash
# Start Redis Stack container
docker run -p 6379:6379 redis/redis-stack:latest
```

## Required Environment Variables

Set these environment variables before running the application:

```bash
export OPENAI_API_KEY=your_openai_api_key
export ANTHROPIC_API_KEY=your_anthropic_api_key  # Optional, for Claude exercises
```

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
   - Model-specific implementations for OpenAI and Claude
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

4. **Services**
   - `RAGService` - High-level API for question answering with context

5. **Configuration**
   - `AppConfig` - Central configuration for vector stores and document processing
   - Profile-based activation of components
   - Data detection to avoid redundant processing

### Profiles

The application uses Spring profiles to enable different features:

- Default: Basic AI chat capabilities
- `rag`: Enables Retrieval-Augmented Generation
- `redis`: Uses Redis as the vector store instead of in-memory

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

## Labs Structure

The project follows a lab-based structure documented in `labs.md`, progressing from:
1. Basic chat interactions
2. Structured data extraction
3. Prompt engineering
4. Memory management
5. Vision and audio capabilities
6. RAG implementation
7. Vector store optimization with Redis