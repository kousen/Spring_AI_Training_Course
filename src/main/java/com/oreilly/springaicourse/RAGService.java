package com.oreilly.springaicourse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    @Autowired
    public RAGService(
            OpenAiChatModel chatModel,
            VectorStore vectorStore) {
        this.chatClient = ChatClient.create(chatModel);
        this.vectorStore = vectorStore;
    }

    public String query(String question) {
        // Define the instruction prompt
        String instructionPrompt = """
                Answer the question based ONLY on the provided context.
                If the context doesn't contain relevant information, say
                "I don't have enough information to answer this question."
                """;

        // Create a QuestionAnswerAdvisor with the vectorStore
        QuestionAnswerAdvisor advisor = new QuestionAnswerAdvisor(vectorStore);

        // Use the advisor to handle the RAG workflow
        return chatClient.prompt()
                .advisors(advisor)
                .system(instructionPrompt)
                .user(question)
                .call()
                .content();
    }
}