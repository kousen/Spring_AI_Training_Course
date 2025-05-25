package com.oreilly.springaicourse;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.Scanner;

@Service
public class RAGService {
    private final ChatClient chatClient;
    private final VectorStore vectorStore;
    private final ChatMemory memory;

    @Autowired
    public RAGService(
            OpenAiChatModel chatModel,
            VectorStore vectorStore, 
            ChatMemory memory) {
        // TODO: Initialize chatClient with appropriate advisors
        // Consider using QuestionAnswerAdvisor for RAG functionality
        // Consider using MessageChatMemoryAdvisor for conversation memory
        this.vectorStore = vectorStore;
        this.memory = memory;
        this.chatClient = ChatClient.create(chatModel); // Basic initialization for main method
    }

    public String query(String question) {
        // TODO: Implement RAG-powered question answering
        // Use queryWithResponse(question).getResult().getOutput().getText()
        return "TODO: Implement RAG functionality";
    }
    
    /**
     * Query the RAG system and return the full ChatResponse with metadata.
     * Useful for testing and accessing document context.
     */
    public ChatResponse queryWithResponse(String question) {
        // TODO: Implement full RAG workflow
        // 1. Create QuestionAnswerAdvisor with vectorStore
        // 2. Create MessageChatMemoryAdvisor with memory
        // 3. Use advisors with chatClient to process question
        // 4. Return full ChatResponse
        return null;
    }

    // Interactive CLI demonstration - shows how the completed RAG system works
    public static void main(String[] args) {
        // Create a Spring application instance
        var app = new SpringApplication(SpringaicourseApplication.class);

        // Set the active profiles to "rag" and "redis"
        app.setAdditionalProfiles("rag", "redis");

        // Start the Spring application and get the application context
        ApplicationContext context = app.run(args);

        // Get the RAGService bean from the context
        RAGService ragService = context.getBean(RAGService.class);

        // Create a Scanner for user input
        Scanner scanner = new Scanner(System.in);

        System.out.println("RAG Question-Answering System");
        System.out.println("Type 'exit' to quit");
        System.out.println("------------------------------");

        // Loop to ask questions until user types 'exit'
        while (true) {
            System.out.print("\nEnter your question: ");
            String question = scanner.nextLine().trim();

            // Check if user wants to exit
            if (question.equalsIgnoreCase("exit") || question.isBlank()) {
                System.out.println("Exiting the application. Goodbye!");
                break;
            }

            try {
                // Query the RAG system and display the response
                System.out.println("\nThinking...");
                String response = ragService.query(question);
                System.out.println("\nResponse:");
                System.out.println(response);
            } catch (Exception e) {
                System.err.println("Error processing your question: " + e.getMessage());
            }
        }

        // Close the scanner
        scanner.close();

        // Exit the application
        System.exit(0);
    }
}