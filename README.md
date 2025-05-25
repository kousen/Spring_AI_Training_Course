# Spring AI Training Course

A hands-on training course for learning Spring AI through progressive lab exercises.

## Getting Started

This repository contains starter code for a comprehensive Spring AI training course. Students build functionality incrementally through guided TODO exercises.

### Repository Structure

- **`main` branch**: Starter code with TODO-guided exercises
- **`solutions` branch**: Complete implementations for reference
- **`labs.md`**: Step-by-step lab instructions and exercises

### Prerequisites

1. **Java 17+**
2. **Environment Variables**:
   ```bash
   export OPENAI_API_KEY=your_openai_api_key
   export ANTHROPIC_API_KEY=your_anthropic_api_key  # Optional
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
2. **Streaming Responses** - Real-time AI responses  
3. **Structured Data Extraction** - AI-powered data parsing
4. **Prompt Templates** - Reusable AI prompts
5. **Chat Memory** - Conversation context
6. **Vision Capabilities** - Image analysis
7. **Image Generation** - AI-created images
8. **AI Tools** - Function calling
9. **Audio Processing** - Speech-to-text and text-to-speech
10. **Retrieval-Augmented Generation (RAG)** - AI with knowledge base
11. **Production RAG** - Redis vector store optimization

## Learning Approach

- **Start with TODOs**: Each test class contains guided TODO comments
- **Build incrementally**: Complete one lab before moving to the next
- **Reference solutions**: Check the `solutions` branch when needed
- **Hands-on learning**: Learn by implementing, not copying

## Support

- **Lab Instructions**: See [labs.md](labs.md)
- **Complete Examples**: Switch to `solutions` branch
- **Issues**: Report problems via GitHub issues