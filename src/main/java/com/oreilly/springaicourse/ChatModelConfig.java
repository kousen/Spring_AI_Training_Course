package com.oreilly.springaicourse;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration to resolve ChatModel ambiguity when multiple models are available.
 * Makes OpenAI the primary chat model for auto-wiring.
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
}