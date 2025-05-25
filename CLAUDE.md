# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a **hands-on training course** for learning Spring AI through progressive lab exercises. The repository is structured as a proper training course where students build functionality incrementally.

### Repository Structure

- **`main` branch**: Starter code with TODO-guided exercises for students
- **`solutions` branch**: Complete working implementations for reference
- **Test classes**: Contain TODO comments guiding students through implementation
- **Service classes**: Skeleton implementations with TODO instructions

The course demonstrates integration of Large Language Models (LLMs) with Spring applications using the Spring AI library (version 1.0.0), covering:

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
# Run all tests (many will be empty TODO stubs in main branch)
./gradlew test

# Run specific test classes (students implement these progressively)
./gradlew test --tests OpenAiTests
./gradlew test --tests ClaudeTests
./gradlew test --tests RAGTests

# Run with specific profiles (for advanced RAG exercises)
./gradlew test --tests RAGTests -Dspring.profiles.active=rag,redis

# To see working tests, switch to solutions branch
git checkout solutions
./gradlew test
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

## Training Course Structure

This is a **hands-on training course** where students implement Spring AI functionality progressively:

### Learning Approach
- **Main branch**: Students start here with TODO-guided starter code
- **Solutions branch**: Complete implementations for reference
- **Progressive labs**: Each lab builds on previous knowledge
- **Hands-on implementation**: Students learn by coding, not copying

### Lab Progression
The course follows a structured progression documented in `labs.md`:
1. **Basic chat interactions** - Simple AI conversations
2. **Streaming responses** - Real-time AI communication
3. **Structured data extraction** - AI-powered data parsing
4. **Prompt engineering** - Template-based prompts
5. **Memory management** - Conversation context
6. **Vision and audio capabilities** - Multimodal AI
7. **RAG implementation** - Knowledge-augmented AI
8. **Vector store optimization** - Production-ready RAG with Redis

### Code Structure for Students
- **Test classes**: Contain TODO comments guiding implementation
- **Service classes**: Skeleton code with clear instructions
- **Working examples**: DateTimeTools, ActorFilms (students use these)
- **Reference implementations**: Available in solutions branch