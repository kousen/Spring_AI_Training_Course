package com.oreilly.springaicourse;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to resolve model ambiguity when multiple providers are available.
 * With both OpenAI and Ollama on the classpath, each contributes a ChatModel
 * and an EmbeddingModel; these @Primary beans make OpenAI the default for
 * auto-wiring. Inject OllamaChatModel directly when you want the local model.
 */
@Configuration
public class ChatModelConfig {

    /**
     * Creates a primary ChatModel bean from OpenAI model.
     * This resolves ambiguity when ChatClient.Builder tries to auto-wire a ChatModel.
     */
    @Bean
    @Primary
    public ChatModel primaryChatModel(@Qualifier("openAiChatModel") OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }

    /**
     * Makes the OpenAI embedding model primary so the RAG vector store
     * doesn't have to choose between OpenAI and Ollama embeddings.
     */
    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(
            @Qualifier("openAiEmbeddingModel") OpenAiEmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }
}